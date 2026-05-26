package dev.lumas.core.model.brigadier;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.lumas.core.annotation.Argument;
import dev.lumas.core.annotation.BrigadierExecutor;
import dev.lumas.core.annotation.Suggests;
import dev.lumas.core.model.MetaHolder;
import dev.lumas.core.util.Logging;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.EntitySelectorArgumentResolver;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Internal helpers for synthesizing a Brigadier tree from the
 * {@link BrigadierExecutor}/{@link Argument} annotation pair. Works for both
 * top-level {@link BrigadierCommand}s and {@link BrigadierSubCommand}s anything
 * exposing a {@link MetaHolder#meta()}.
 * <p>
 * Supports linear chains with optional suffix arguments, plus per-argument
 * tab-completion suppliers via {@link Suggests @Suggests} methods on the same class.
 */
@NullMarked
@SuppressWarnings("UnstableApiUsage")
final class BrigadierTrees {

    private BrigadierTrees() {}

    static LiteralArgumentBuilder<CommandSourceStack> buildAnnotatedTree(MetaHolder holder) {
        Method executor = findExecutor(holder);
        if (executor == null) {
            throw new IllegalStateException(
                    holder.getClass().getName() + " must either override buildTree(Commands) " +
                            "or declare a method annotated with @BrigadierExecutor"
            );
        }
        return buildAnnotatedTree(holder, executor);
    }

    private static @Nullable Method findExecutor(MetaHolder holder) {
        Method found = null;
        for (Method method : holder.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(BrigadierExecutor.class)) {
                if (found != null) {
                    throw new IllegalStateException(
                            holder.getClass().getName() + " has multiple @BrigadierExecutor methods; only one is allowed"
                    );
                }
                found = method;
            }
        }
        return found;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildAnnotatedTree(
            MetaHolder holder,
            Method executor
    ) {
        Parameter[] params = executor.getParameters();
        if (params.length == 0 || !CommandSourceStack.class.isAssignableFrom(params[0].getType())) {
            throw new IllegalStateException(
                    "@BrigadierExecutor method " + executor + " must take CommandSourceStack as its first parameter"
            );
        }

        validateParameters(executor, params);

        if (!executor.canAccess(holder)) {
            executor.setAccessible(true);
        }
        
        Map<String, Method> suggesters = collectSuggesters(holder, params);

        String literal = holder.meta().name();
        
        if (params.length == 1) {
            return Commands.literal(literal).executes(invocation(holder, executor, params, 0));
        }
        
        List<RequiredArgumentBuilder<CommandSourceStack, ?>> nodes = new ArrayList<>(params.length - 1);
        for (int i = 1; i < params.length; i++) {
            Argument arg = params[i].getAnnotation(Argument.class);
            ArgumentType<?> type = resolveArgumentType(arg, params[i]);
            RequiredArgumentBuilder<CommandSourceStack, ?> node = Commands.argument(arg.value(), type);

            Method suggester = suggesters.get(arg.value());
            if (suggester != null) {
                node.suggests(makeSuggestionProvider(holder, suggester));
            }

            nodes.add(node);
        }

        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal(literal);

        // The literal itself is also a stop point if the first argument is optional -
        // i.e. the command can be invoked with zero arguments supplied.
        if (params[1].getAnnotation(Argument.class).optional()) {
            root.executes(invocation(holder, executor, params, 0));
        }

        // Attach an executor at every position where the command can validly stop:
        //   - the last node (always),
        //   - any node immediately before an optional node.
        // The executor at depth `d` invokes the method with args 1..d from the context
        // and null for the rest.
        for (int i = 0; i < nodes.size(); i++) {
            boolean isLast = i == nodes.size() - 1;
            boolean nextIsOptional = !isLast && params[i + 2].getAnnotation(Argument.class).optional();
            if (isLast || nextIsOptional) {
                int depth = i + 1; // number of args supplied at this stop point
                nodes.get(i).executes(invocation(holder, executor, params, depth));
            }
        }

        // Fold right-to-left: each outer node .then(inner).
        ArgumentBuilder<CommandSourceStack, ?> current = nodes.get(nodes.size() - 1);
        for (int i = nodes.size() - 2; i >= 0; i--) {
            current = nodes.get(i).then(current);
        }

        return root.then(current);
    }

    private static void validateParameters(Method executor, Parameter[] params) {
        boolean seenOptional = false;
        for (int i = 1; i < params.length; i++) {
            Argument arg = params[i].getAnnotation(Argument.class);
            if (arg == null) {
                throw new IllegalStateException(
                        "Parameter '" + params[i].getName() + "' of " + executor +
                                " must be annotated with @Argument"
                );
            }

            if (arg.optional()) {
                if (params[i].getType().isPrimitive()) {
                    throw new IllegalStateException(
                            "Optional @Argument(\"" + arg.value() + "\") on " + executor +
                                    " uses primitive type " + params[i].getType().getName() +
                                    " - use the boxed type (e.g. Double, Integer) so null can be passed when omitted"
                    );
                }
                seenOptional = true;
            } else if (seenOptional) {
                throw new IllegalStateException(
                        "Required @Argument(\"" + arg.value() + "\") on " + executor +
                                " appears after an optional argument. All optional arguments must come last."
                );
            }
        }
    }

    /**
     * Find all {@link Suggests @Suggests}-annotated methods on the holder, indexed
     * by argument name. Validates each one:
     * <ul>
     *     <li>References an actual {@code @Argument} on the executor</li>
     *     <li>Has the expected signature {@code (CommandContext<CommandSourceStack>, SuggestionsBuilder) → CompletableFuture<Suggestions>}</li>
     *     <li>No two @Suggests methods target the same argument</li>
     * </ul>
     */
    private static Map<String, Method> collectSuggesters(MetaHolder holder, Parameter[] params) {
        // Build the set of valid argument names from the executor signature.
        Set<String> validArgNames = new HashSet<>();
        for (int i = 1; i < params.length; i++) {
            validArgNames.add(params[i].getAnnotation(Argument.class).value());
        }

        Map<String, Method> result = new HashMap<>();
        for (Method method : holder.getClass().getDeclaredMethods()) {
            Suggests annotation = method.getAnnotation(Suggests.class);
            if (annotation == null) {
                continue;
            }

            String argName = annotation.value();

            if (!validArgNames.contains(argName)) {
                throw new IllegalStateException(
                        "@Suggests(\"" + argName + "\") on " + method +
                                " does not match any @Argument on the executor of " +
                                holder.getClass().getName() +
                                ". Valid names: " + validArgNames
                );
            }

            if (result.containsKey(argName)) {
                throw new IllegalStateException(
                        "Multiple @Suggests(\"" + argName + "\") methods on " +
                                holder.getClass().getName() + "; only one is allowed per argument"
                );
            }

            validateSuggesterSignature(method);

            if (!method.canAccess(holder)) {
                method.setAccessible(true);
            }
            result.put(argName, method);
        }
        return result;
    }

    private static void validateSuggesterSignature(Method method) {
        Class<?>[] paramTypes = method.getParameterTypes();
        if (paramTypes.length != 2
                || !CommandContext.class.isAssignableFrom(paramTypes[0])
                || !SuggestionsBuilder.class.isAssignableFrom(paramTypes[1])) {
            throw new IllegalStateException(
                    "@Suggests method " + method +
                            " must have signature (CommandContext<CommandSourceStack>, SuggestionsBuilder)"
            );
        }
        if (!CompletableFuture.class.isAssignableFrom(method.getReturnType())) {
            throw new IllegalStateException(
                    "@Suggests method " + method +
                            " must return CompletableFuture<Suggestions>"
            );
        }
    }

    /**
     * Wrap a {@link Suggests @Suggests}-annotated method as a Brigadier
     * {@link SuggestionProvider}.
     */
    @SuppressWarnings("unchecked")
    private static SuggestionProvider<CommandSourceStack> makeSuggestionProvider(
            MetaHolder holder,
            Method suggester
    ) {
        return (ctx, builder) -> {
            try {
                Object result = suggester.invoke(holder, ctx, builder);
                if (result instanceof CompletableFuture<?> future) {
                    return (CompletableFuture<Suggestions>) future;
                }
                Logging.warningLog(
                        "@Suggests method " + suggester + " returned non-CompletableFuture: " +
                                (result == null ? "null" : result.getClass().getName())
                );
                return builder.buildFuture();
            } catch (Exception e) {
                Throwable cause = e.getCause() != null ? e.getCause() : e;
                Logging.errorLog("@Suggests method " + suggester + " threw", cause);
                return builder.buildFuture();
            }
        };
    }

    /**
     * Build a {@link Command} that invokes {@code executor} with {@code depth} args
     * pulled from the context (positions 1..depth) and {@code null} for the rest.
     */
    private static Command<CommandSourceStack> invocation(
            MetaHolder holder,
            Method executor,
            Parameter[] params,
            int depth
    ) {
        return ctx -> {
            try {
                @org.jetbrains.annotations.Nullable
                Object[] callArgs = new Object[params.length];
                callArgs[0] = ctx.getSource();
                for (int i = 1; i < params.length; i++) {
                    if (i <= depth) {
                        Argument arg = params[i].getAnnotation(Argument.class);
                        callArgs[i] = resolveArgument(ctx, arg.value(), params[i]);
                    } else {
                        callArgs[i] = null;
                    }
                }
                executor.invoke(holder, callArgs);
                return Command.SINGLE_SUCCESS;
            } catch (Exception e) {
                Throwable cause = e.getCause() != null ? e.getCause() : e;
                if (cause instanceof CommandSyntaxException cse) {
                    throw cse;
                }
                throw new RuntimeException(cause);
            }
        };
    }

    /**
     * Pull an argument out of the {@link CommandContext}, bridging Paper resolver
     * types to their declared Bukkit equivalents:
     * <ul>
     *     <li>{@code Player} / {@code Entity} → first resolved selector match (or null)</li>
     *     <li>{@code List<Player>} / {@code List<Entity>} → full resolved list</li>
     * </ul>
     */
    private static @Nullable Object resolveArgument(
            CommandContext<CommandSourceStack> ctx,
            String name,
            Parameter parameter
    ) throws CommandSyntaxException {
        Class<?> paramType = parameter.getType();

        if (paramType.equals(Player.class)) {
            PlayerSelectorArgumentResolver resolver =
                    ctx.getArgument(name, PlayerSelectorArgumentResolver.class);
            List<Player> resolved = resolver.resolve(ctx.getSource());
            return resolved.isEmpty() ? null : resolved.getFirst();
        }
        if (paramType.equals(Entity.class)) {
            EntitySelectorArgumentResolver resolver =
                    ctx.getArgument(name, EntitySelectorArgumentResolver.class);
            List<Entity> resolved = resolver.resolve(ctx.getSource());
            return resolved.isEmpty() ? null : resolved.getFirst();
        }

        // List<Player> / List<Entity> - return the full resolved list.
        if (List.class.isAssignableFrom(paramType)) {
            Class<?> element = ArgumentTypeInference.listElementType(parameter);
            if (Player.class.equals(element)) {
                PlayerSelectorArgumentResolver resolver =
                        ctx.getArgument(name, PlayerSelectorArgumentResolver.class);
                return resolver.resolve(ctx.getSource());
            }
            if (Entity.class.equals(element)) {
                EntitySelectorArgumentResolver resolver =
                        ctx.getArgument(name, EntitySelectorArgumentResolver.class);
                return resolver.resolve(ctx.getSource());
            }
        }

        return ctx.getArgument(name, paramType);
    }

    private static ArgumentType<?> resolveArgumentType(Argument arg, Parameter parameter) {
        Class<? extends ArgumentTypeProvider> providerClass = arg.provider();
        if (!providerClass.equals(ArgumentTypeProvider.None.class)) {
            try {
                ArgumentTypeProvider provider = providerClass.getDeclaredConstructor().newInstance();
                return provider.provide();
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException(
                        "Failed to instantiate ArgumentTypeProvider " + providerClass.getName(), e
                );
            }
        }

        Class<? extends ArgumentType<?>> typeClass = arg.type();
        if (!typeClass.equals(Argument.DefaultArgumentType.class)) {
            return instantiateArgumentType(typeClass);
        }

        ArgumentType<?> inferred = ArgumentTypeInference.infer(parameter);
        if (inferred == null) {
            throw new IllegalStateException(
                    "Could not infer ArgumentType for parameter '" + arg.value() +
                            "' of type " + parameter.getParameterizedType().getTypeName() +
                            " - specify @Argument(type=...) or @Argument(provider=...)"
            );
        }
        return inferred;
    }

    private static ArgumentType<?> instantiateArgumentType(Class<? extends ArgumentType<?>> typeClass) {
        try {
            var field = typeClass.getDeclaredField("INSTANCE");
            if (Modifier.isStatic(field.getModifiers()) && typeClass.isAssignableFrom(field.getType())) {
                field.setAccessible(true);
                Object value = field.get(null);
                if (value instanceof ArgumentType<?> at) {
                    return at;
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
            // fall through to constructor
        }

        try {
            return typeClass.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(
                    "Failed to instantiate ArgumentType " + typeClass.getName() +
                            ", provide a public no-args constructor or a public static INSTANCE field, " +
                            "or use @Argument(provider=...) instead",
                    e
            );
        }
    }
}
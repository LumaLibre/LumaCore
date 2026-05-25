package dev.lumas.core.model.brigadier;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.lumas.core.annotation.Argument;
import dev.lumas.core.annotation.BrigadierExecutor;
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
import java.util.List;

/**
 * Internal helpers for synthesizing a Brigadier tree from the
 * {@link BrigadierExecutor}/{@link Argument} annotation pair. Works for both
 * top-level {@link BrigadierCommand}s and {@link BrigadierSubCommand}s  anything
 * exposing a {@link MetaHolder#meta()}.
 * <p>
 * Supports linear chains with optional suffix arguments. An optional argument
 * attaches an executor to the preceding node, letting the user stop the command
 * there; the executor method is then invoked with {@code null} for any omitted
 * optional argument.
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

        String literal = holder.meta().name();

        // No args  the literal itself is the executor.
        if (params.length == 1) {
            return Commands.literal(literal).executes(invocation(holder, executor, params, 0));
        }

        // Build argument nodes in declaration order.
        List<RequiredArgumentBuilder<CommandSourceStack, ?>> nodes = new ArrayList<>(params.length - 1);
        for (int i = 1; i < params.length; i++) {
            Argument arg = params[i].getAnnotation(Argument.class);
            ArgumentType<?> type = resolveArgumentType(arg, params[i]);
            nodes.add(Commands.argument(arg.value(), type));
        }

        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal(literal);

        // The literal itself is also a stop point if the first argument is optional 
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
                                    "  use the boxed type (e.g. Double, Integer) so null can be passed when omitted"
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
                @org.jetbrains.annotations.Nullable Object[] callArgs = new Object[params.length];
                callArgs[0] = ctx.getSource();
                for (int i = 1; i < params.length; i++) {
                    if (i <= depth) {
                        Argument arg = params[i].getAnnotation(Argument.class);
                        callArgs[i] = resolveArgument(ctx, arg.value(), params[i].getType());
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
                Logging.errorLog("Failed to invoke @BrigadierExecutor on " + holder.getClass().getName(), cause);
                return 0;
            }
        };
    }

    /**
     * Pull an argument out of the {@link CommandContext}, bridging Paper resolver
     * types to their declared Bukkit equivalents.
     * <p>
     * Currently bridges:
     * <ul>
     *     <li>{@link PlayerSelectorArgumentResolver} → {@link Player} (first match,
     *         or {@code null} if the selector matched nothing)</li>
     *     <li>{@link EntitySelectorArgumentResolver} → {@link Entity} (first match,
     *         or {@code null} if the selector matched nothing)</li>
     * </ul>
     * Other types pass through with a direct {@code ctx.getArgument(name, paramType)}.
     */
    private static @Nullable Object resolveArgument(
            CommandContext<CommandSourceStack> ctx,
            String name,
            Class<?> paramType
    ) throws CommandSyntaxException {
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

        ArgumentType<?> inferred = ArgumentTypeInference.infer(parameter.getType());
        if (inferred == null) {
            throw new IllegalStateException(
                    "Could not infer ArgumentType for parameter '" + arg.value() +
                            "' of type " + parameter.getType().getName() +
                            "  specify @Argument(type=...) or @Argument(provider=...)"
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
                            " - provide a public no-args constructor or a public static INSTANCE field, " +
                            "or use @Argument(provider=...) instead",
                    e
            );
        }
    }
}
package dev.lumas.core.model.brigadier;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.predicate.ItemStackPredicate;
import io.papermc.paper.command.brigadier.argument.range.DoubleRangeProvider;
import io.papermc.paper.command.brigadier.argument.range.IntegerRangeProvider;
import io.papermc.paper.entity.LookAnchor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import org.bukkit.GameMode;
import org.bukkit.HeightMap;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.structure.Mirror;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Maps common Java parameter types to their natural {@link ArgumentType} for the
 * annotation-driven path of {@link BrigadierSubCommand}.
 * <p>
 * Paper resolver-returning arguments (e.g. {@link ArgumentTypes#player()},
 * {@link ArgumentTypes#entity()}) are bridged at invocation time by
 * {@link BrigadierTrees}, so users can declare the Bukkit type directly on the
 * parameter.
 *
 * <h2>Types that need explicit opt-in</h2>
 * Some Paper argument types are intentionally NOT in the table because they would
 * be ambiguous or would surprise users:
 * <ul>
 *     <li>{@link ArgumentTypes#players()} / {@link ArgumentTypes#entities()} 
 *         same Java return types as the singular variants. Use
 *         {@code @Argument(provider=...)} if you need the multi-selector.</li>
 *     <li>{@link ArgumentTypes#time()}  returns {@link Integer}, would silently
 *         turn every {@code int} parameter into a tick-parsing argument.</li>
 *     <li>{@link ArgumentTypes#resource(io.papermc.paper.registry.RegistryKey)} /
 *         {@link ArgumentTypes#resourceKey(io.papermc.paper.registry.RegistryKey)}
 *          require a registry key parameter, can't be no-args inferred.</li>
 *     <li>Position/rotation resolvers ({@code blockPosition()},
 *         {@code finePosition()}, {@code rotation()})  need
 *         {@code CommandSourceStack} to resolve and don't have a single canonical
 *         Java type.</li>
 *     <li>{@code signedMessage()}, {@code playerProfiles()}  non-trivial
 *         resolution models.</li>
 * </ul>
 */
@NullMarked
@SuppressWarnings("UnstableApiUsage")
final class ArgumentTypeInference {

    private static final Map<Class<?>, Supplier<ArgumentType<?>>> MAPPINGS = new HashMap<>();

    static {
        // Primitives + wrappers
        MAPPINGS.put(int.class, IntegerArgumentType::integer);
        MAPPINGS.put(Integer.class, IntegerArgumentType::integer);

        MAPPINGS.put(long.class, LongArgumentType::longArg);
        MAPPINGS.put(Long.class, LongArgumentType::longArg);

        MAPPINGS.put(float.class, FloatArgumentType::floatArg);
        MAPPINGS.put(Float.class, FloatArgumentType::floatArg);

        MAPPINGS.put(double.class, DoubleArgumentType::doubleArg);
        MAPPINGS.put(Double.class, DoubleArgumentType::doubleArg);

        MAPPINGS.put(boolean.class, BoolArgumentType::bool);
        MAPPINGS.put(Boolean.class, BoolArgumentType::bool);

        MAPPINGS.put(String.class, StringArgumentType::word);

        MAPPINGS.put(Player.class, ArgumentTypes::player);
        MAPPINGS.put(Entity.class, ArgumentTypes::entity);

        MAPPINGS.put(World.class, ArgumentTypes::world);
        MAPPINGS.put(GameMode.class, ArgumentTypes::gameMode);
        MAPPINGS.put(HeightMap.class, ArgumentTypes::heightMap);
        MAPPINGS.put(UUID.class, ArgumentTypes::uuid);
        MAPPINGS.put(Criteria.class, ArgumentTypes::objectiveCriteria);
        MAPPINGS.put(DisplaySlot.class, ArgumentTypes::scoreboardDisplaySlot);
        MAPPINGS.put(LookAnchor.class, ArgumentTypes::entityAnchor);
        MAPPINGS.put(Mirror.class, ArgumentTypes::templateMirror);
        MAPPINGS.put(StructureRotation.class, ArgumentTypes::templateRotation);

        // Items / blocks
        MAPPINGS.put(ItemStack.class, ArgumentTypes::itemStack);
        MAPPINGS.put(ItemStackPredicate.class, ArgumentTypes::itemPredicate);
        MAPPINGS.put(BlockState.class, ArgumentTypes::blockState);

        // Text / keys
        MAPPINGS.put(Component.class, ArgumentTypes::component);
        MAPPINGS.put(Style.class, ArgumentTypes::style);
        MAPPINGS.put(NamedTextColor.class, ArgumentTypes::namedColor);
        MAPPINGS.put(NamespacedKey.class, ArgumentTypes::namespacedKey);
        MAPPINGS.put(Key.class, ArgumentTypes::key);

        // Ranges
        MAPPINGS.put(IntegerRangeProvider.class, ArgumentTypes::integerRange);
        MAPPINGS.put(DoubleRangeProvider.class, ArgumentTypes::doubleRange);
    }

    private ArgumentTypeInference() {}

    static @Nullable ArgumentType<?> infer(Class<?> type) {
        Supplier<ArgumentType<?>> supplier = MAPPINGS.get(type);
        return supplier != null ? supplier.get() : null;
    }
}
package dev.lumas.core.util;

import net.kyori.adventure.text.format.TextColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class PluginContextLogger extends ContextLogger {

    private static final StackWalker PLUGIN_STACK_WALKER =
            StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

    private final @Nullable String pluginClassName;
    private final @Nullable String pluginName;

    protected PluginContextLogger(
            String callerClassName,
            String simpleCallerClassName,
            @Nullable TextColor standardColor,
            boolean deep,
            @Nullable String pluginClassName,
            @Nullable String pluginName
    ) {
        super(callerClassName, simpleCallerClassName, standardColor, deep);
        this.pluginClassName = pluginClassName;
        this.pluginName = pluginName;
    }

    public @Nullable String getPluginClassName() {
        return pluginClassName;
    }

    public @Nullable String getPluginName() {
        return pluginName;
    }

    private static @Nullable JavaPlugin findPluginFromStack(int skipFrames) {
        return PLUGIN_STACK_WALKER.walk(frames ->
                frames.skip(skipFrames)
                        .map(StackWalker.StackFrame::getDeclaringClass)
                        .filter(JavaPlugin.class::isAssignableFrom)
                        .findFirst()
                        .map(cls -> {
                            try {
                                return JavaPlugin.getProvidingPlugin(cls);
                            } catch (IllegalArgumentException e) {
                                return null;
                            }
                        })
                        .orElse(null)
        );
    }

    private static PluginContextLogger createLogger(@Nullable TextColor color, boolean deep, int skipFrames) {
        // Reuse parent's stack logic for caller info
        ContextLogger base = getLoggerInternal(color, deep, skipFrames);

        JavaPlugin plugin = findPluginFromStack(skipFrames);
        String pluginClassName = plugin != null ? plugin.getClass().getName() : null;
        String pluginName = plugin != null ? plugin.getName() : null;

        return new PluginContextLogger(
                base.getCallerClassName(),
                base.getSimpleCallerClassName(),
                color,
                deep,
                pluginClassName,
                pluginName
        );
    }

    public static PluginContextLogger getPluginLogger(@Nullable TextColor color, boolean deep) {
        return createLogger(color, deep, BASE_SKIP_DEPTH + 1);
    }

    public static PluginContextLogger getPluginLogger(@Nullable TextColor color) {
        return createLogger(color, false, BASE_SKIP_DEPTH + 1);
    }

    public static PluginContextLogger getPluginLogger(boolean deep) {
        return createLogger(null, deep, BASE_SKIP_DEPTH + 1);
    }

    public static PluginContextLogger getPluginLogger() {
        return createLogger(null, false, BASE_SKIP_DEPTH + 1);
    }
}
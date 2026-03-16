package dev.lumas.lumacore.utility;

import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.Nullable;

/**
 * @deprecated Use {@link dev.lumas.core.util.objects.ContextLogger}
 */
@Deprecated
public class ContextLogger extends dev.lumas.core.util.objects.ContextLogger {

    private ContextLogger(String callerClassName, String simpleCallerClassName, @Nullable TextColor standardColor, boolean deep) {
        super(callerClassName, simpleCallerClassName, standardColor, deep);
    }

    private static ContextLogger wrap(dev.lumas.core.util.objects.ContextLogger logger) {
        return new ContextLogger(
                logger.getCallerClassName(),
                logger.getSimpleCallerClassName(),
                logger.getStandardColor(),
                logger.isDeep()
        );
    }

    public static ContextLogger getLogger(@Nullable TextColor color, boolean deep) {
        return wrap(dev.lumas.core.util.objects.ContextLogger.getLogger(color, deep));
    }

    public static ContextLogger getLogger(@Nullable TextColor color) {
        return wrap(dev.lumas.core.util.objects.ContextLogger.getLogger(color));
    }

    public static ContextLogger getLogger(boolean deep) {
        return wrap(dev.lumas.core.util.objects.ContextLogger.getLogger(deep));
    }

    public static ContextLogger getLogger() {
        return wrap(dev.lumas.core.util.objects.ContextLogger.getLogger());
    }
}
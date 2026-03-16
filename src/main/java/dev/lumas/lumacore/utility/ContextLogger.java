package dev.lumas.lumacore.utility;

import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.Nullable;

/**
 * @deprecated Use {@link dev.lumas.core.util.ContextLogger}
 */
@Deprecated
public class ContextLogger extends dev.lumas.core.util.ContextLogger {

    private ContextLogger(String callerClassName, String simpleCallerClassName, @Nullable TextColor standardColor, boolean deep) {
        super(callerClassName, simpleCallerClassName, standardColor, deep);
    }

    public static ContextLogger getLogger(@Nullable TextColor color, boolean deep) {
        var l = dev.lumas.core.util.ContextLogger.getLoggerInternal(color, deep, BASE_SKIP_DEPTH);
        return new ContextLogger(l.getCallerClassName(), l.getSimpleCallerClassName(), l.getStandardColor(), l.isDeep());
    }

    public static ContextLogger getLogger(@Nullable TextColor color) {
        var l = dev.lumas.core.util.ContextLogger.getLoggerInternal(color, false, BASE_SKIP_DEPTH);
        return new ContextLogger(l.getCallerClassName(), l.getSimpleCallerClassName(), l.getStandardColor(), l.isDeep());
    }

    public static ContextLogger getLogger(boolean deep) {
        var l = dev.lumas.core.util.ContextLogger.getLoggerInternal(null, deep, BASE_SKIP_DEPTH);
        return new ContextLogger(l.getCallerClassName(), l.getSimpleCallerClassName(), l.getStandardColor(), l.isDeep());
    }

    public static ContextLogger getLogger() {
        var l = dev.lumas.core.util.ContextLogger.getLoggerInternal(null, false, BASE_SKIP_DEPTH);
        return new ContextLogger(l.getCallerClassName(), l.getSimpleCallerClassName(), l.getStandardColor(), l.isDeep());
    }
}
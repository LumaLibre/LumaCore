package dev.lumas.lumacore.utility;

import lombok.Getter;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;

@Getter
public class LogUtil {

    private static final StackWalker STACK_WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

    private final String callerClassName;
    private final String simpleCallerClassName;
    private final String callerMethodName;

    private LogUtil() {
        StackWalker.StackFrame frame = STACK_WALKER.walk(frames -> frames.skip(1).findFirst()).orElse(null);
        if (frame != null) {
            this.callerClassName = frame.getClassName();
            this.simpleCallerClassName = frame.getDeclaringClass().getSimpleName();
            this.callerMethodName = frame.getMethodName();
        } else {
            this.callerClassName = "UnknownClass";
            this.simpleCallerClassName = "UnknownClass";
            this.callerMethodName = "unknownMethod";
        }
    }


    private void logInternal(@Nullable TextColor textColor, String msg, boolean deep) {
        String prefix = !deep ? "[%s] ".formatted(simpleCallerClassName) : "[%s::%s] ".formatted(callerClassName, callerMethodName);

        Bukkit.getConsoleSender().sendMessage(Text.mm(prefix + msg).color(textColor));
    }

    private void logInternal(String msg, boolean deep) {
        logInternal(null, msg, deep);
    }

    private void logInternal(String msg) {
        logInternal(msg, false);
    }

    private void logThrowable(@Nullable TextColor textColor, String msg, Throwable throwable) {
        logInternal(textColor, msg, true);
        logInternal(textColor, throwable.toString(), false);
        for (StackTraceElement ste : throwable.getStackTrace()) {
            String str = ste.toString();
            if (str.contains(".jar//")) {
                str = str.substring(str.indexOf(".jar//") + 6);
            }
            logInternal(textColor, str, false);
        }
        Throwable cause = throwable.getCause();
        while (cause != null) {
            logInternal(textColor, "Caused by: " + cause, false);
            for (StackTraceElement ste : cause.getStackTrace()) {
                String str = ste.toString();
                if (str.contains(".jar//")) {
                    str = str.substring(str.indexOf(".jar//") + 6);
                }
                logInternal(textColor, str, false);
            }
            cause = cause.getCause();
        }
    }

    public void info(String msg) {
        logInternal(msg);
    }

    public void debug(String msg) {
        logInternal(msg);
    }

    public void debug(String msg, Throwable throwable) {
        logThrowable(null, msg, throwable);
    }

    public void warning(String msg) {
        logInternal(NamedTextColor.GOLD, msg, false);
    }

    public void warning(String msg, Throwable throwable) {
        logThrowable(NamedTextColor.GOLD, msg, throwable);
    }

    public void error(String msg) {
        logInternal(NamedTextColor.RED, msg, false);
    }

    public void error(String msg, Throwable throwable) {
        logThrowable(NamedTextColor.RED, msg, throwable);
    }


    public static LogUtil getLogger() {
        return new LogUtil();
    }
}

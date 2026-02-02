package dev.lumas.lumacore.utility;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
public class ContextLogger {

    private static final StackWalker STACK_WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

    private final String callerClassName;
    private final String simpleCallerClassName;
    private boolean deep;

    private ContextLogger(String callerClassName, String simpleCallerClassName, boolean deep) {
        this.callerClassName = callerClassName;
        this.simpleCallerClassName = simpleCallerClassName;
        this.deep = deep;
    }


    public void log(@Nullable TextColor textColor, String msg, int depth) {
        String prefix = "[%s] ".formatted(simpleCallerClassName);


        final int finalDepth = deep && depth == 0 ? 3 : depth;

        if (finalDepth > 0) {
            StackWalker.StackFrame frame = STACK_WALKER.walk(frames ->
                    frames.skip(1 + finalDepth).findFirst()
            ).orElse(null);

            String callerMethodName = frame != null ? frame.getMethodName() : "UnknownMethod";
            prefix = "[%s::%s] ".formatted(simpleCallerClassName, callerMethodName);
        }

        Bukkit.getConsoleSender().sendMessage(Text.mm(prefix + msg).color(textColor));
    }


    public void log(@Nullable TextColor textColor, String msg) {
        log(textColor, msg, 0);
    }

    public void log(String msg, int depth) {
        log(null, msg, depth);
    }

    public void log(String msg) {
        log(msg, 0);
    }

    public void logThrowable(@Nullable TextColor textColor, String msg, Throwable throwable) {
        log(textColor, msg, 2);
        log(textColor, throwable.toString());
        for (StackTraceElement ste : throwable.getStackTrace()) {
            String str = ste.toString();
            if (str.contains(".jar//")) {
                str = str.substring(str.indexOf(".jar//") + 6);
            }
            log(textColor, str);
        }
        Throwable cause = throwable.getCause();
        while (cause != null) {
            log(textColor, "Caused by: " + cause);
            for (StackTraceElement ste : cause.getStackTrace()) {
                String str = ste.toString();
                if (str.contains(".jar//")) {
                    str = str.substring(str.indexOf(".jar//") + 6);
                }
                log(textColor, str);
            }
            cause = cause.getCause();
        }
    }

    public void info(String msg) {
        log(msg);
    }

    public void debug(String msg) {
        log(msg);
    }

    public void debug(String msg, Throwable throwable) {
        logThrowable(null, msg, throwable);
    }

    public void warning(String msg) {
        log(NamedTextColor.GOLD, msg, 1);
    }

    public void warning(String msg, Throwable throwable) {
        logThrowable(NamedTextColor.GOLD, msg, throwable);
    }

    public void error(String msg) {
        log(NamedTextColor.RED, msg, 1);
    }

    public void error(String msg, Throwable throwable) {
        logThrowable(NamedTextColor.RED, msg, throwable);
    }


    public static ContextLogger getLogger(boolean deep) {
        StackWalker.StackFrame frame = STACK_WALKER.walk(frames -> frames.skip(1).findFirst()).orElse(null);

        String callerClassName = frame != null ? frame.getClassName() : "UnknownClass";
        String simpleCallerClassName = frame != null ? frame.getDeclaringClass().getSimpleName() : "UnknownClass";

        return new ContextLogger(callerClassName, simpleCallerClassName, deep);
    }

    public static ContextLogger getLogger() {
        return getLogger(false);
    }
}

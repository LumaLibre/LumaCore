package dev.lumas.lumacore.utility;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

/**
 * @deprecated Use {@link dev.lumas.core.util.Logging}
 */
@Deprecated
public class Logging {

    public static void msg(CommandSender sender, String msg) {
        dev.lumas.core.util.Logging.msg(sender, msg);
    }

    public static void log(String msg) {
        dev.lumas.core.util.Logging.log(msg);
    }

    public static void log(LogLevel level, String msg) {
        dev.lumas.core.util.Logging.log(level.toNew(), msg);
    }

    public static void log(LogLevel level, String msg, @Nullable Throwable throwable) {
        dev.lumas.core.util.Logging.log(level.toNew(), msg, throwable);
    }

    public static void warningLog(String msg) {
        dev.lumas.core.util.Logging.warningLog(msg);
    }

    public static void errorLog(String msg) {
        dev.lumas.core.util.Logging.errorLog(msg);
    }

    public static void errorLog(String msg, Throwable throwable) {
        dev.lumas.core.util.Logging.errorLog(msg, throwable);
    }

    @Deprecated
    public enum LogLevel {
        INFO,
        WARNING,
        ERROR,
        DEBUG;

        public dev.lumas.core.util.Logging.LogLevel toNew() {
            return dev.lumas.core.util.Logging.LogLevel.valueOf(name());
        }
    }
}
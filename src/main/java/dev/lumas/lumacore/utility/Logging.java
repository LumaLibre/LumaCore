package dev.lumas.lumacore.utility;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

public class Logging {

    public static void msg(CommandSender sender, String msg) {
        sender.sendMessage(Text.mm("[LumaCore] " + msg));
    }

    public static void log(String msg) {
        Bukkit.getConsoleSender().sendMessage(Text.mm("[LumaCore] " + msg));
    }

    public static void log(LogLevel level, String msg) {
        log(level, msg, null);
    }

    public static void log(LogLevel level, String msg, @Nullable Throwable throwable) {
        switch (level) {
            case INFO -> log(msg);
            case WARNING -> warningLog(msg);
            case ERROR -> {
                if (throwable != null) {
                    errorLog(msg, throwable);
                } else {
                    errorLog(msg);
                }
            }
            case DEBUG -> {}
        }
    }

    public static void warningLog(String msg) {
        Bukkit.getConsoleSender().sendMessage(Text.mm("<yellow>[LumaCore] WARNING: " + msg));
    }

    public static void errorLog(String msg) {
        Component str = Text.mm("<red>[LumaCore] ERROR: " + msg);
        Bukkit.getConsoleSender().sendMessage(str);
    }


    public static void errorLog(String msg, Throwable throwable) {
        errorLog(msg);
        errorLog("<gold>" + throwable.toString());
        for (StackTraceElement ste : throwable.getStackTrace()) {
            String str = ste.toString();
            if (str.contains(".jar//")) {
                str = str.substring(str.indexOf(".jar//") + 6);
            }
            errorLog(str);
        }
        Throwable cause = throwable.getCause();
        while (cause != null) {
            Bukkit.getConsoleSender().sendMessage(Text.mm("<red>[LumaCore]<gold> Caused by: " + cause));
            for (StackTraceElement ste : cause.getStackTrace()) {
                String str = ste.toString();
                if (str.contains(".jar//")) {
                    str = str.substring(str.indexOf(".jar//") + 6);
                }
                Bukkit.getConsoleSender().sendMessage(Text.mm("<red>[LumaCore]<gold>      " + str));
            }
            cause = cause.getCause();
        }
    }


    public enum LogLevel {
        INFO,
        WARNING,
        ERROR,
        DEBUG
    }
}

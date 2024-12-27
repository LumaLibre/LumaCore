package dev.jsinco.luma.lumacore.utility;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

public class Logging {
    public enum LogLevel {
        INFO,
        WARNING,
        ERROR,
        DEBUG
    }

    public static Component color(String msg) {
        return MiniMessage.miniMessage().deserialize(msg);
    }

    public static void msg(CommandSender sender, String msg) {
        sender.sendMessage(color("[LumaCore] " + msg));
    }

    public static void log(String msg) {
        Bukkit.getConsoleSender().sendMessage(color("[LumaCore] " + msg));
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
        Bukkit.getConsoleSender().sendMessage(color("<yellow>[LumaCore] WARNING: " + msg));
    }

    public static void errorLog(String msg) {
        Component str = color("<red>[LumaCore] ERROR: " + msg);
        Bukkit.getConsoleSender().sendMessage(str);
    }

    // TODO: cleanup
    public static void errorLog(String msg, Throwable throwable) {
        errorLog(msg);
        errorLog("&6" + throwable.toString());
        for (StackTraceElement ste : throwable.getStackTrace()) {
            String str = ste.toString();
            if (str.contains(".jar//")) {
                str = str.substring(str.indexOf(".jar//") + 6);
            }
            errorLog(str);
        }
        Throwable cause = throwable.getCause();
        while (cause != null) {
            Bukkit.getConsoleSender().sendMessage(color("<red>[LumaCore]<gold> Caused by: " + cause));
            for (StackTraceElement ste : cause.getStackTrace()) {
                String str = ste.toString();
                if (str.contains(".jar//")) {
                    str = str.substring(str.indexOf(".jar//") + 6);
                }
                Bukkit.getConsoleSender().sendMessage(color("<red>[LumaCore]<gold>      " + str));
            }
            cause = cause.getCause();
        }
    }
}

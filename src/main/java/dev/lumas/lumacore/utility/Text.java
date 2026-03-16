package dev.lumas.lumacore.utility;

import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * @deprecated Use {@link dev.lumas.core.util.Text}
 */
@Deprecated
public class Text {

    public static final Component PREFIX = dev.lumas.core.util.Text.PREFIX;

    public static void msg(CommandSender sender, String m) {
        dev.lumas.core.util.Text.msg(sender, m);
    }

    public static void msg(CommandSender sender, Component m) {
        dev.lumas.core.util.Text.msg(sender, m);
    }

    public static Component mm(String m) {
        return dev.lumas.core.util.Text.mm(m);
    }

    public static Component mmNoItalic(String m) {
        return dev.lumas.core.util.Text.mmNoItalic(m);
    }

    public static List<Component> mml(String m) {
        return dev.lumas.core.util.Text.mml(m);
    }

    public static List<Component> mml(Component... m) {
        return dev.lumas.core.util.Text.mml(m);
    }

    public static List<Component> mmlNoItalic(String m) {
        return dev.lumas.core.util.Text.mmlNoItalic(m);
    }

    public static List<Component> mml(List<String> m) {
        return dev.lumas.core.util.Text.mml(m);
    }

    public static List<Component> mmlNoItalic(List<String> m) {
        return dev.lumas.core.util.Text.mmlNoItalic(m);
    }

    public static List<Component> mml(String... m) {
        return dev.lumas.core.util.Text.mml(m);
    }

    public static List<Component> mmlNoItalic(String... m) {
        return dev.lumas.core.util.Text.mmlNoItalic(m);
    }

    public static String toMonoUpperText(String input) {
        return dev.lumas.core.util.Text.toMonoUpperText(input);
    }
}
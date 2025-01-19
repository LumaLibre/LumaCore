package dev.jsinco.luma.lumacore.utility;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.stream.Stream;

public class Text {

    public static final Component PREFIX = mm("<b><#b986f9>Info</b> <dark_gray>»<white> ");

    private static final String NORMAL_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String MONO_UPPER_ALPHABET = "ᴀʙᴄᴅᴇꜰɢʜɪᴊᴋʟᴍɴᴏᴘQʀꜱᴛᴜᴠᴡxʏᴢ";

    public static void msg(CommandSender sender, String m) {
        sender.sendMessage(PREFIX.append(mm(m)));
    }

    public static void msg(CommandSender sender, Component m) {
        sender.sendMessage(PREFIX.append(m));
    }

    public static Component mm(String m) {
        return MiniMessage.miniMessage().deserialize(m);
    }

    public static Component mmNoItalic(String m) {
        return MiniMessage.miniMessage().deserialize("<!i>" + m);
    }

    public static List<Component> mml(String m) {
        return List.of(mm(m));
    }

    public static List<Component> mml(List<String> m) {
        return m.stream().map(Text::mm).toList();
    }

    public static List<Component> mml(String... m) {
        return Stream.of(m).map(Text::mm).toList();
    }

    public static String toMonoUpperText(String input) {
        if (input == null) {
            return null;
        }
        input = input.toUpperCase();

        StringBuilder result = new StringBuilder();

        for (char c : input.toCharArray()) {
            int index = NORMAL_ALPHABET.indexOf(c);

            if (index >= 0) {
                result.append(MONO_UPPER_ALPHABET.charAt(index));
            } else {
                // If not a letter, just append the original character
                result.append(c);
            }
        }

        return result.toString();
    }
}

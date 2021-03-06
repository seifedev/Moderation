package tech.seife.moderation.utils;

import org.bukkit.ChatColor;
import tech.seife.moderation.Moderation;
import tech.seife.moderation.enums.ReplaceType;

import java.util.Map;

public class MessageManager {

    public static String getTranslatedMessageWithReplace(Moderation plugin, String path, Map<ReplaceType, String> values) {
        if (plugin.getDataHandler().getCustomFiles() != null && plugin.getDataHandler().getCustomFiles().getTranslationConfig() != null && plugin.getDataHandler().getCustomFiles().getTranslationConfig().getString(path) != null) {
            String message = plugin.getDataHandler().getCustomFiles().getTranslationConfig().getString(path);

            for (Map.Entry<ReplaceType, String> entry : values.entrySet()) {
                message = message.replaceAll(entry.getKey().getValue(), entry.getValue());
            }
            return ChatColor.translateAlternateColorCodes('&', message);
        }
        return "There isn't a message.";
    }

    public static String getTranslatedMessage(Moderation plugin, String path) {
        if (plugin.getDataHandler().getCustomFiles() != null && plugin.getDataHandler().getCustomFiles().getTranslationConfig() != null && plugin.getDataHandler().getCustomFiles().getTranslationConfig().getString(path) != null) {
            return ChatColor.translateAlternateColorCodes('&', plugin.getDataHandler().getCustomFiles().getTranslationConfig().getString(path));
        }
        return null;
    }
}

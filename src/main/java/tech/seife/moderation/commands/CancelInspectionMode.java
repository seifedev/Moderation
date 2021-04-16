package tech.seife.moderation.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tech.seife.moderation.Moderation;
import tech.seife.moderation.utils.MessageManager;

public class CancelInspectionMode implements CommandExecutor {

    private final Moderation plugin;

    public CancelInspectionMode(Moderation plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            ((Player) sender).setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
            sender.sendMessage(MessageManager.getTranslatedMessage(plugin, "cancelInspection"));
        }
        return true;
    }
}

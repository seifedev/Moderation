package tech.seife.moderation.commands;

import tech.seife.moderation.Moderation;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Who implements CommandExecutor {

    private final Moderation plugin;

    public Who(Moderation plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (plugin.getConfig().getConfigurationSection("whoRanks") != null) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                for (String rankName : plugin.getConfig().getConfigurationSection("whoRanks").getKeys(false)) {
                    if (player.hasPermission("moderation.whoRanks." + rankName)) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getConfigurationSection("whoRanks").getString(rankName)) + "Rank: " + rankName + " player: " + player.getName());
                        break;
                    }
                }
            }
        }
        return true;
    }
}

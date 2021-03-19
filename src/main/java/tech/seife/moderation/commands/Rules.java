package tech.seife.moderation.commands;

import tech.seife.moderation.Moderation;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Rules implements CommandExecutor {

    private final Moderation plugin;

    public Rules(Moderation plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            if (plugin.getConfig().get("RuleBook") != null && plugin.getConfig().isItemStack("RuleBook")) {
                ((Player) sender).getInventory().addItem(plugin.getConfig().getItemStack("RuleBook"));
            }
        }
        return true;
    }
}

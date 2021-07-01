package tech.seife.moderation.commands.spy;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ViewEnderChest implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 1 || Bukkit.getPlayer(args[0]) == null) return true;

        if (sender instanceof Player) {
            ((Player) sender).openInventory(Bukkit.getPlayer(args[0]).getEnderChest());
        }
        return true;
    }
}

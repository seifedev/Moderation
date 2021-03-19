package tech.seife.moderation.commands.bans;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tech.seife.moderation.Moderation;
import tech.seife.moderation.guis.BansHistoryInventory;
import tech.seife.moderation.utils.MojangApiQuery;

public class ViewPlayerBanHistory implements CommandExecutor {

    private final Moderation plugin;

    public ViewPlayerBanHistory(Moderation plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;

        if (args.length == 1 && args[0] != null) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {

                BansHistoryInventory bansHistoryInventory = new BansHistoryInventory(plugin, ((Player) sender), args[0], args[0]);

                Bukkit.getScheduler().runTask(plugin, () -> bansHistoryInventory.openInventory(((Player) sender)));
            });
        }

        return true;
    }
}

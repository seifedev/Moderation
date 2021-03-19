package tech.seife.moderation.commands.bans;

import tech.seife.moderation.Moderation;
import tech.seife.moderation.datamanager.dao.DataManager;
import tech.seife.moderation.utils.MojangApiQuery;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.UUID;


public class RemoveBan implements CommandExecutor {

    private final Moderation plugin;
    private final DataManager dataManager;

    public RemoveBan(Moderation plugin, DataManager dataManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 1 || args[0] == null) return true;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            dataManager.removeBan(dataManager.retrieveCurrentBannedPlayerInformation(args[0]));
        });
        return true;
    }
}


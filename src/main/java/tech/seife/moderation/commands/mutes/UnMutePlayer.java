package tech.seife.moderation.commands.mutes;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import tech.seife.chatutilities.channels.ChannelManager;
import tech.seife.moderation.datamanager.dao.DataManager;

public class UnMutePlayer implements CommandExecutor {

    private final ChannelManager channelManager;
    private final DataManager dataManager;

    public UnMutePlayer(DataManager dataManager, ChannelManager channelManager) {
        this.dataManager = dataManager;
        this.channelManager = channelManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 2 || args[0] == null || Bukkit.getPlayer(args[0]) == null || args[1] == null || channelManager.getChannel(args[1]) == null)
            return true;

        dataManager.removeMute(Bukkit.getPlayer(args[0]).getName(), args[1]);

        return true;
    }
}

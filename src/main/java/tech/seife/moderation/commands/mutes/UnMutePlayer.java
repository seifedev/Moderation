package tech.seife.moderation.commands.mutes;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tech.seife.chatutilities.channels.ChannelManager;
import tech.seife.moderation.Moderation;
import tech.seife.moderation.datamanager.dao.DataManager;
import tech.seife.moderation.enums.ReplaceType;
import tech.seife.moderation.utils.MessageManager;

import java.util.HashMap;
import java.util.Map;

public class UnMutePlayer implements CommandExecutor {

    private final Moderation plugin;
    private final ChannelManager channelManager;
    private final DataManager dataManager;

    public UnMutePlayer(Moderation plugin, DataManager dataManager, ChannelManager channelManager) {
        this.dataManager = dataManager;
        this.channelManager = channelManager;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 2 || args[0] == null || Bukkit.getPlayer(args[0]) == null || args[1] == null || channelManager.getChannel(args[1]) == null)
            return true;

        Player player = Bukkit.getPlayer(args[0]);
        String channelName = channelManager.getChannel(args[1]).getName();

        if (this.dataManager.isPlayerMutedByUsername(player.getDisplayName(), channelName)) {

            dataManager.removeMute(player.getDisplayName(), channelName);

            sender.sendMessage(getMessage(sender, player, channelName, "unMuteSender"));
            sender.sendMessage(getMessage(sender, player, channelName, "unMuteReceiver"));

        }
        return true;
    }

    private String getMessage(CommandSender sender, Player mutedPlayer, String channelName, String path) {
        Map<ReplaceType, String> values = new HashMap<>();

        if (path.contains("sender")) {
            values.put(ReplaceType.PLAYER_NAME, mutedPlayer.getName());
        } else {
            values.put(ReplaceType.PLAYER_NAME, sender.getName());
        }

        values.put(ReplaceType.Channel, channelName);

        return MessageManager.getTranslatedMessageWithReplace(plugin, path, values);
    }
}

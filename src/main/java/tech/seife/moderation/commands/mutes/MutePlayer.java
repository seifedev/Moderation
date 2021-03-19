package tech.seife.moderation.commands.mutes;

import tech.seife.moderation.Moderation;
import tech.seife.moderation.utils.ParseDate;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class MutePlayer implements CommandExecutor {

    private final Moderation plugin;

    public MutePlayer(Moderation plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player && args.length > 1 && args[0] != null && Bukkit.getPlayer(args[0]) != null) {

            Player mutedBy = ((Player) sender);
            Player mutedPlayer = Bukkit.getPlayer(args[0]);

            if (args.length == 3 && args[1] != null && args[2] != null && plugin.getChatUtilities().getChannelManager().getChannel(args[1]) != null) {

                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME;
                LocalDateTime muteReleaseDate = LocalDateTime.parse(ParseDate.transformInputToDateInstant(args[2]), dateTimeFormatter);

                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> plugin.getMutedPlayerManager().addMutedPlayer(mutedBy.getUniqueId(), mutedPlayer.getUniqueId(), mutedBy.getName(), mutedPlayer.getName(), plugin.getChatUtilities().getChannelManager().getChannel(args[1]).getName(), LocalDateTime.now(), muteReleaseDate));

                mutedPlayer.sendMessage("You have been muted in " + plugin.getChatUtilities().getChannelManager().getChannel(args[1]).getName() +" for " + args[2]);
                mutedBy.sendMessage(mutedPlayer.getDisplayName() + " has been muted in: " + plugin.getChatUtilities().getChannelManager().getChannel(args[1]).getName() + " for " +  args[2]);
            } else if (args.length == 2 && args[1] != null) {
                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME;
                LocalDateTime muteReleaseDate = LocalDateTime.parse(ParseDate.transformInputToDateInstant(args[1]), dateTimeFormatter);

                plugin.getChatUtilities().getChannelManager().getChannels()
                        .forEach(channel -> {
                            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> plugin.getMutedPlayerManager().addMutedPlayer(mutedBy.getUniqueId(), mutedPlayer.getUniqueId(), mutedBy.getName(), mutedPlayer.getName(), channel.getName(), LocalDateTime.now(), muteReleaseDate));
                            mutedPlayer.sendMessage("You have been muted in " + channel.getName() +" for " + args[1]);
                            mutedBy.sendMessage(mutedPlayer.getDisplayName() + " has been muted in: " + channel.getName() + " for " + args[1]);
                        });
            }
        }

        return true;
    }
}

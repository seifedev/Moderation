package tech.seife.moderation.commands;

import tech.seife.moderation.Moderation;
import tech.seife.moderation.ModerationScoreboard;
import tech.seife.moderation.utils.MessageManager;
import tech.seife.moderation.utils.MojangApiQuery;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class InspectPlayer implements CommandExecutor {

    private final Moderation plugin;

    public InspectPlayer(Moderation plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player && args.length == 1 && args[0] != null) {
            ModerationScoreboard scoreboard = new ModerationScoreboard(plugin);

            scoreboard.setScoreboard(((Player) sender), Bukkit.getPlayer(MojangApiQuery.getPlayersUuidFromName(args[0])));
            sender.sendMessage(MessageManager.getTranslatedMessage(plugin, "inspectPlayer"));
        }
        return true;
    }
}

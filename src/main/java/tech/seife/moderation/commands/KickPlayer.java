package tech.seife.moderation.commands;

import tech.seife.moderation.Moderation;
import tech.seife.moderation.enums.ReplaceType;
import tech.seife.moderation.utils.MessageManager;
import tech.seife.moderation.datamanager.dao.DataManager;
import tech.seife.moderation.datamanager.kicks.KickManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class KickPlayer implements CommandExecutor {

    private final Moderation plugin;
    private final DataManager dataManager;


    public KickPlayer(Moderation plugin, DataManager dataManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;

        if (args.length < 2 || Bukkit.getPlayer(args[0]) == null) {
            sender.sendMessage("You must specify reason.");
            return true;
        }

        Player player = Bukkit.getPlayer(args[0]);

        String kickMessage = getKickedReason(args);

        kickPlayer(player, sender.getName(), kickMessage);


        KickManager kickManager = new KickManager(dataManager);
        kickManager.addKick(player.getUniqueId(), ((Player) sender).getUniqueId(), player.getName(), sender.getName(), kickMessage, LocalDateTime.now());

        return true;
    }

    private void kickPlayer(Player kickedPlayer, String kickedBy, String reason) {
        Map<ReplaceType, String> values = new HashMap<>();

        values.put(ReplaceType.PLAYER_NAME, kickedBy);
        values.put(ReplaceType.REASON, reason);

        kickedPlayer.kickPlayer(MessageManager.getTranslatedMessageWithReplace(plugin, "kickedMessage", values));

    }


    private String getKickedReason(String[] args) {
        StringBuilder sb = new StringBuilder();

        for (int i = 2; i < args.length; i++) {
            sb.append(args[i]);
        }
        return sb.toString();
    }

}

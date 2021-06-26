package tech.seife.moderation.commands.bans;

import net.sf.cglib.core.Local;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tech.seife.moderation.Moderation;
import tech.seife.moderation.enums.ReplaceType;
import tech.seife.moderation.utils.MessageManager;
import tech.seife.moderation.utils.ParseDate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class BanPlayer implements CommandExecutor {

    private final Moderation plugin;

    public BanPlayer(Moderation plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        if (args.length < 3 || Bukkit.getPlayer(args[0]) == null) return true;

        Player player = Bukkit.getPlayer(args[0]);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME;
        LocalDateTime banReleaseDate = LocalDateTime.parse(ParseDate.transformInputToDateInstant(args[1]), dateTimeFormatter).plusHours(3);

        String bannedReason = getBanReason(args);

        kickPlayer(player, sender.getName(), bannedReason, banReleaseDate);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> plugin.getBannedPlayerManager().addBannedPlayer(generateBanId(), ((Player) sender).getUniqueId(), player.getUniqueId(), sender.getName(), player.getName(), bannedReason, LocalDateTime.now(), banReleaseDate));
        return true;
    }

    private void kickPlayer(Player bannedPlayer, String bannedBy, String reason, LocalDateTime localDateTime) {
        Map<ReplaceType, String> values = new HashMap<>();

        values.put(ReplaceType.PLAYER_NAME, bannedBy);
        values.put(ReplaceType.REASON, reason);
        values.put(ReplaceType.DATE, localDateTime.toString());

        bannedPlayer.kickPlayer(MessageManager.getTranslatedMessageWithReplace(plugin, "banPlayer", values));

    }

    private String getBanReason(String[] args) {
        StringBuilder sb = new StringBuilder();

        for (int i = 2; i < args.length; i++) {
            sb.append(args[i]);
        }
        return sb.toString();
    }

    private int generateBanId() {
        return plugin.getDataHandler().getDataManager().getLastBanId() + 1;
    }
}

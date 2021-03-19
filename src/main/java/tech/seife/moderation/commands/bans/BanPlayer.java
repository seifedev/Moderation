package tech.seife.moderation.commands.bans;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tech.seife.moderation.Moderation;
import tech.seife.moderation.utils.ParseDate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.regex.Pattern;

public class BanPlayer implements CommandExecutor {

    private static final Pattern PATTERN = Pattern.compile("(?:([0-9]+)\\s*y[a-z]*[,\\s]*)?" + "(?:([0-9]+)\\s*mo[a-z]*[,\\s]*)?" + "(?:([0-9]+)\\s*w[a-z]*[,\\s]*)?" + "(?:([0-9]+)\\s*d[a-z]*[,\\s]*)?" + "(?:([0-9]+)\\s*h[a-z]*[,\\s]*)?" + "(?:([0-9]+)\\s*m[a-z]*[,\\s]*)?" + "(?:([0-9]+)\\s*(?:s[a-z]*)?)?", Pattern.CASE_INSENSITIVE);
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
        LocalDateTime banReleaseDate = LocalDateTime.parse(ParseDate.transformInputToDateInstant(args[1]), dateTimeFormatter);

        String banReason = plugin.getMessageManager().createBanMessage(args);

        player.kickPlayer("You have been banned for: " + banReason +
                "\nThe ban expires at: " + banReleaseDate);

        String message = plugin.getMessageManager().replaceDate(plugin.getMessageManager().replacePlayerName(Objects.requireNonNull("test"), player.getName()), banReleaseDate);
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> plugin.getBannedPlayerManager().addBannedPlayer(generateBanId(), ((Player) sender).getUniqueId(), player.getUniqueId(), ((Player) sender).getName(), player.getName(), banReason, LocalDateTime.now(), banReleaseDate));
        return true;
    }

    private int generateBanId() {
        return plugin.getDataManager().getLastBanId() + 1;
    }
}

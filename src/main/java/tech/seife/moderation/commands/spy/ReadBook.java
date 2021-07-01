package tech.seife.moderation.commands.spy;

import tech.seife.moderation.Moderation;
import tech.seife.moderation.datamanager.dao.CachedData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tech.seife.moderation.utils.MessageManager;

import java.util.UUID;

public class ReadBook implements CommandExecutor {

    private final CachedData cachedData;
    private final Moderation plugin;


    public ReadBook(Moderation plugin, CachedData cachedData) {
        this.plugin = plugin;
        this.cachedData = cachedData;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 1 || args[0] == null || cachedData.getBooks().get(UUID.fromString(args[0])) == null)
            return true;

        if (sender instanceof Player) {
            Player player = (Player) sender;
            player.getInventory().addItem(cachedData.getBooks().get(UUID.fromString(args[0])));
            cachedData.removeBooks(UUID.fromString(args[0]));

            player.sendMessage(MessageManager.getTranslatedMessage(plugin, "receivedBook"));
        }
        return true;
    }
}

package tech.seife.moderation.commands.spy;

import tech.seife.moderation.Moderation;
import tech.seife.moderation.datamanager.dao.CachedData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tech.seife.moderation.utils.MessageManager;

public class DisableSpyCommand implements CommandExecutor {

    private final CachedData cachedData;
    private final Moderation plugin;

    public DisableSpyCommand(Moderation plugin, CachedData cachedData) {
        this.cachedData = cachedData;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (sender instanceof Player) {
            cachedData.getSpyMembers().removeIf(uuid -> ((Player) sender).getUniqueId().equals(uuid));
            sender.sendMessage(MessageManager.getTranslatedMessage(plugin, "disabledSpy"));
        }
        return true;
    }
}

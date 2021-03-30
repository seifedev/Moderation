package tech.seife.moderation.commands.spy;

import tech.seife.moderation.Moderation;
import tech.seife.moderation.datamanager.dao.CachedData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tech.seife.moderation.utils.MessageManager;

public class EnableSpyCommand implements CommandExecutor {

    private final CachedData cachedData;
    private final Moderation plugin;

    public EnableSpyCommand(Moderation plugin, CachedData cachedData) {
        this.cachedData = cachedData;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            cachedData.getSpyMembers().add(((Player) sender).getUniqueId());
            sender.sendMessage(MessageManager.getTranslatedMessage(plugin, "enableSpy"));
        }
        return true;
    }
}

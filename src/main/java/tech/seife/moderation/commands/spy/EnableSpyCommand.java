package tech.seife.moderation.commands.spy;

import tech.seife.moderation.datamanager.dao.CachedData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EnableSpyCommand implements CommandExecutor {

    private final CachedData cachedData;

    public EnableSpyCommand(CachedData cachedData) {
        this.cachedData = cachedData;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            cachedData.getSpyMembers().add(((Player) sender).getUniqueId());
        }
        return true;
    }
}

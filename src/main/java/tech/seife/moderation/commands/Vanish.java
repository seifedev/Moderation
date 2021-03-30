package tech.seife.moderation.commands;

import com.comphenix.protocol.wrappers.EnumWrappers;
import tech.seife.moderation.Moderation;
import tech.seife.moderation.packets.RemoveFromTab;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tech.seife.moderation.utils.MessageManager;

public class Vanish implements CommandExecutor {

    private final Moderation plugin;

    public Vanish(Moderation plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;

        if (!plugin.getCachedData().getOnVanishModePlayers().contains(((Player) sender).getUniqueId())) {

            plugin.getCachedData().getOnVanishModePlayers().add(player.getUniqueId());

            RemoveFromTab removeFromTab = new RemoveFromTab(plugin, plugin.getProtocolManager(), plugin.getCachedData());
            removeFromTab.sendPackets(player, EnumWrappers.PlayerInfoAction.REMOVE_PLAYER);
        } else {
            plugin.getCachedData().getOnVanishModePlayers().remove(player.getUniqueId());
            RemoveFromTab removeFromTab = new RemoveFromTab(plugin, plugin.getProtocolManager(), plugin.getCachedData());
            removeFromTab.sendPackets(player, EnumWrappers.PlayerInfoAction.ADD_PLAYER);
        }
        sender.sendMessage(MessageManager.getTranslatedMessage(plugin, "invisible"));

        return true;
    }

}

package tech.seife.moderation.commands;

import com.comphenix.protocol.PacketType;
import tech.seife.moderation.Moderation;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tech.seife.moderation.utils.MessageManager;

public class SetRules implements CommandExecutor {

    private final Moderation plugin;

    public SetRules(Moderation plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (player.getInventory().getItemInMainHand().getType().equals(Material.WRITTEN_BOOK)) {
                plugin.getConfig().set("RuleBook", player.getInventory().getItemInMainHand());
                sender.sendMessage(MessageManager.getTranslatedMessage(plugin, "newRules"));
                plugin.saveConfig();
            }
        }
        return true;
    }
}

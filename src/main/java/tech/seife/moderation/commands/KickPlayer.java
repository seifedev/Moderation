package tech.seife.moderation.commands;

import tech.seife.moderation.MessageManager;
import tech.seife.moderation.datamanager.dao.DataManager;
import tech.seife.moderation.datamanager.kicks.KickManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.util.Objects;

public class KickPlayer implements CommandExecutor {

    private final MessageManager messageManager;
    private final FileConfiguration config;
    private final DataManager dataManager;


    public KickPlayer(MessageManager messageManager, FileConfiguration config, DataManager dataManager) {
        this.messageManager = messageManager;
        this.config = config;
        this.dataManager = dataManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;

        if (args.length < 2 || Bukkit.getPlayer(args[0]) == null) {
            sender.sendMessage("You must specify reason.");
            return true;
        };

        Player player = Bukkit.getPlayer(args[0]);

        String kickMessage = messageManager.createKickMessage(args);

        Objects.requireNonNull(player).kickPlayer("\n\n" + "kicked by: " + sender.getName() + "\n\nReason: " + kickMessage);


        KickManager kickManager = new KickManager(dataManager);
        kickManager.addKick(player.getUniqueId(), ((Player) sender).getUniqueId(), player.getName(), ((Player) sender).getName(), kickMessage, LocalDateTime.now());

        return true;
    }
}

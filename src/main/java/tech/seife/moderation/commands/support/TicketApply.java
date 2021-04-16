package tech.seife.moderation.commands.support;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tech.seife.moderation.Moderation;
import tech.seife.moderation.datamanager.dao.DataManager;
import tech.seife.moderation.datamanager.tickets.Ticket;
import tech.seife.moderation.datamanager.tickets.TicketManager;
import tech.seife.moderation.utils.MessageManager;

public class TicketApply implements CommandExecutor {

    private final Moderation plugin;
    private final DataManager dataManager;
    private final TicketManager ticketManager;

    public TicketApply(Moderation plugin, DataManager dataManager, TicketManager ticketManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
        this.ticketManager = ticketManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player && args.length >= 1) {
            Player player = (Player) sender;

            if (player.getInventory().getItemInMainHand().getType().equals(Material.WRITTEN_BOOK)) {
                Ticket ticket = ticketManager.transformBookToTicket(player, player.getInventory().getItemInMainHand());
                dataManager.saveTicket(ticket);
                sender.sendMessage(MessageManager.getTranslatedMessage(plugin, "submitTicket"));
            } else {
                sender.sendMessage("You must be holding a signed book.");
            }
        } else {
            sender.sendMessage("Please specify a title.");
        }
        return true;
    }
}

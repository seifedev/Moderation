package tech.seife.moderation.commands.support;

import tech.seife.moderation.datamanager.dao.DataManager;
import tech.seife.moderation.datamanager.tickets.Ticket;
import tech.seife.moderation.datamanager.tickets.TicketManager;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TicketApply implements CommandExecutor {

    private final DataManager dataManager;
    private final TicketManager ticketManager;

    public TicketApply(DataManager dataManager, TicketManager ticketManager) {
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
                sender.sendMessage("ticket submitted");
            }
        }
        return true;
    }
}

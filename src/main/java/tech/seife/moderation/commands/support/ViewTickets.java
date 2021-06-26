package tech.seife.moderation.commands.support;

import tech.seife.moderation.Moderation;
import tech.seife.moderation.guis.TicketHistoryInventory;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Set;

public class ViewTickets implements CommandExecutor {

    private final Moderation plugin;

    public ViewTickets(Moderation plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (args.length == 0) {
                openTicketInventory(player, player);

            } else if (sender.hasPermission("moderation.ticketHistory.others") && args.length == 1 && args[0] != null) {
                Player playerToInspect = Bukkit.getPlayer(args[0]);

                if (playerToInspect != null) {
                    openTicketInventory(player, playerToInspect);

                }
            }
        }

        return true;
    }

    private void openTicketInventory(Player playerToOpenInventory, Player playerToInspect) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Set<Integer> ticketIds = plugin.getDataHandler().getDataManager().retrieveTicketsIdForPlayer(playerToInspect.getName());
            if (ticketIds != null) {
                TicketHistoryInventory ticketHistoryGui = new TicketHistoryInventory(plugin, playerToOpenInventory, playerToInspect.getName());

                ticketHistoryGui.createTicketsBooks(playerToOpenInventory.getUniqueId(), ticketIds, playerToOpenInventory.getName());

                Bukkit.getScheduler().runTask(plugin, () -> ticketHistoryGui.openInventory(playerToOpenInventory));
            }
        });
    }
}

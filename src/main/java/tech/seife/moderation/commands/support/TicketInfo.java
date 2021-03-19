package tech.seife.moderation.commands.support;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class TicketInfo implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        sender.sendMessage(ticketInfoMessage());
        return true;
    }

    private String ticketInfoMessage() {
        return "/helpme [message] to sumbit help ticket \n" +
                "/helphistory to view your ticket history";
    }
}

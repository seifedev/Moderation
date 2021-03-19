package tech.seife.moderation;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MessageManager {

    public String replacePlayerName(String message, String playerName) {
        return message.replaceAll("%player%", playerName);
    }

    public String replaceDate(String message, LocalDateTime LocalDateTime) {
        return message.replaceAll("%date%", LocalDateTime.format(DateTimeFormatter.ISO_DATE_TIME));
    }

    public String createKickMessage(String[] args) {
        StringBuilder builder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            builder.append(args[i]);
        }
        return builder.toString();
    }

    public String createBanMessage(String[] args) {
        StringBuilder builder = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
            builder.append(args[i]);
        }
        return builder.toString();
    }


}

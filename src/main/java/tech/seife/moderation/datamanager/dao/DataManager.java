package tech.seife.moderation.datamanager.dao;

import tech.seife.moderation.datamanager.banned.BannedPlayer;
import tech.seife.moderation.datamanager.kicks.Kick;
import tech.seife.moderation.datamanager.mutes.MutedPlayer;
import tech.seife.moderation.datamanager.spiedtext.SpiedText;
import tech.seife.moderation.datamanager.tickets.Ticket;

import java.util.Set;
import java.util.UUID;

public interface DataManager {

    void saveBan(BannedPlayer bannedPlayer);

    void removeBan(BannedPlayer bannedPlayer);

    boolean isPlayerBannedUuidCheck(UUID playerUuid);

    boolean isPlayerBannedUsernameCheck(String playerUsername);

    Set<BannedPlayer> loadPlayerBanHistory(String playerUsername);

    BannedPlayer retrieveCurrentBannedPlayerInformation(String playerUsername);

    int getLastBanId();

    void moveCurrentBanToBanHistory(int banId);

    int getTotalBannedTimesForPlayer(String playerUsername);

    void saveKick(Kick kick);

    int getLastKickedId();

    int getKickedTimesForPlayer(String playerName);

    void saveTextFromChat(SpiedText spiedText);

    Set<SpiedText> retrieveSpiedText(String playerUsername);

    void saveTicket(Ticket ticket);

    int getAmountOfTickets(String playerUsername);

    Set<Integer> retrieveTicketsIdForPlayer(String playerUsername);

    Ticket retrieveTicket(int id, String playerUsername);

    boolean verifyTicketId(int id, String playerUsername);

    int getLastTicketId();

    void saveMute(MutedPlayer mutedPlayer);

    void removeMute(String playerUsername, String channelName);

    boolean isPlayerMutedByUuid(UUID playerUuid, String channelName);

    boolean isPlayerMutedByUsername(String playerUsername, String channelName);

    MutedPlayer loadMutedPlayer(String playerUsername, String channelName);

    int getTotalMutedTimesForPlayer(String playerUsername);

    int getLastMuteId();
}

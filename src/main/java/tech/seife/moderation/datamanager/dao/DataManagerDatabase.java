package tech.seife.moderation.datamanager.dao;

import tech.seife.moderation.Moderation;
import tech.seife.moderation.datamanager.banned.BannedPlayer;
import tech.seife.moderation.datamanager.kicks.Kick;
import tech.seife.moderation.datamanager.mutes.MutedPlayer;
import tech.seife.moderation.datamanager.spiedtext.SpiedText;
import tech.seife.moderation.datamanager.tickets.Ticket;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class DataManagerDatabase implements DataManager {
    private final Moderation plugin;

    public DataManagerDatabase(Moderation plugin) {
        this.plugin = plugin;
    }

    @Override
    public void saveBan(BannedPlayer bannedPlayer) {
        int banId = insertIntoBansAndAcquireId(bannedPlayer);

        if (banId != -1) {
            String sqlQuery = "INSERT INTO current_bans(ban_id) VALUE (?);";

            try (Connection connection = plugin.getConnectionPoolManager().getConnection();
                 PreparedStatement ps = connection.prepareStatement(sqlQuery)) {

                ps.setInt(1, banId);

                ps.executeUpdate();

            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to save ban!\nError: " + e.getMessage());
            }
        }
    }

    private int insertIntoBansAndAcquireId(BannedPlayer bannedPlayer) {
        String sqlQuery = "INSERT INTO bans (banned_by_uuid, banned_by_username, player_uuid, player_username, banned_reason, banned_date, release_date) VALUES (?, ?, ?, ?, ?, ?, ?);";

        try (Connection connection = plugin.getConnectionPoolManager().getConnection();
             PreparedStatement ps = connection.prepareStatement(sqlQuery, Statement.RETURN_GENERATED_KEYS)) {

            int bannedDateId = saveDateAndGetId(bannedPlayer.getBannedDate());
            int releaseDateId = saveDateAndGetId(bannedPlayer.getReleaseDate());

            ps.setString(1, bannedPlayer.getBannedByUuid().toString());
            ps.setString(2, bannedPlayer.getBannedByName());
            ps.setString(3, bannedPlayer.getBannedUuid().toString());
            ps.setString(4, bannedPlayer.getBannedPlayerName());
            ps.setString(5, bannedPlayer.getReason());
            ps.setInt(6, bannedDateId);
            ps.setInt(7, releaseDateId);

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();

            return rs.next() ? rs.getInt(1) : -1;

        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to acquire ban id!\nError: " + e.getMessage());
        }
        return -1;
    }

    @Override
    public void removeBan(BannedPlayer bannedPlayer) {
        if (isPlayerBannedUuidCheck(bannedPlayer.getBannedUuid())) {
            int banId = acquireBanId(bannedPlayer.getBannedPlayerName());

            if (banId != -1) {
                moveCurrentBanToBanHistory(banId);
            }
        }
    }

    @Override
    public BannedPlayer retrieveCurrentBannedPlayerInformation(String playerUsername) {
        int banId = acquireBanId(playerUsername);

        if (banId == -1) return null;

        String sqlQuery = "SELECT * FROM bans WHERE id = ?;";

        try (Connection connection = plugin.getConnectionPoolManager().getConnection();
             PreparedStatement ps = connection.prepareStatement(sqlQuery)) {

            ps.setInt(1, banId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return translateBanFromResultSet(rs);
            }

        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to retrieve banned player information!\nError message: " + e.getMessage());

        }
        return null;
    }

    @Override
    public int getLastBanId() {
        String sqlQuery = "SELECT * FROM bans ORDER BY id DESC LIMIT 1;";

        try (Connection connection = plugin.getConnectionPoolManager().getConnection();
             PreparedStatement ps = connection.prepareStatement(sqlQuery)) {

            ResultSet rs = ps.executeQuery();

            return rs.next() ? rs.getInt("id") : -1;

        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to retrieve last banned id!\nError message: " + e.getMessage());
        }

        return -1;
    }

    private int acquireBanId(String playerUsername) {
        String sqlQuery = "SELECT *" +
                "FROM bans" +
                "         INNER JOIN current_bans cb WHERE cb.id = bans.id" +
                "  and bans.player_username = ?;";

        try (Connection connection = plugin.getConnectionPoolManager().getConnection();
             PreparedStatement ps = connection.prepareStatement(sqlQuery, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, playerUsername);

            ResultSet rs = ps.executeQuery();

            return rs.next() ? rs.getInt(1) : -1;

        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to acquire current bans id!: \nError message: " + e.getMessage());
        }
        return -1;
    }

    @Override
    public boolean isPlayerBannedUuidCheck(UUID bannedPlayerUuid) {
        String sqlQuery = "SELECT * FROM current_bans cb INNER JOIN bans  WHERE cb.id = bans.id and bans.player_uuid = ?;";

        try (Connection connection = plugin.getConnectionPoolManager().getConnection();
             PreparedStatement ps = connection.prepareStatement(sqlQuery)) {

            ps.setString(1, bannedPlayerUuid.toString());

            ResultSet rs = ps.executeQuery();

            return rs.next();

        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to check if the player is banned!\nError message: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean isPlayerBannedUsernameCheck(String playerUsername) {
        String sqlQuery = "SELECT * FROM current_bans cb INNER JOIN bans  WHERE cb.id = bans.id and bans.player_username = ?;";

        try (Connection connection = plugin.getConnectionPoolManager().getConnection();
             PreparedStatement ps = connection.prepareStatement(sqlQuery)) {

            ps.setString(1, playerUsername);

            ResultSet rs = ps.executeQuery();

            return rs.next();

        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to check if the player is banned!\nError message: " + e.getMessage());
        }
        return false;
    }

    @Override
    public Set<BannedPlayer> loadPlayerBanHistory(String playerUsername) {
        String sqlQuery = "SELECT * FROM bans where player_username = ?";

        try (Connection connection = plugin.getConnectionPoolManager().getConnection();
             PreparedStatement ps = connection.prepareStatement(sqlQuery)) {

            ps.setString(1, playerUsername);

            ResultSet rs = ps.executeQuery();

            Set<BannedPlayer> bannedPlayerHistory = new HashSet<>();

            while (rs.next()) {
                bannedPlayerHistory.add(translateBanFromResultSet(rs));
            }

            return bannedPlayerHistory;

        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Couldn't load player ban history!\nError message: " + e.getMessage());
        }
        return null;
    }

    private BannedPlayer translateBanFromResultSet(ResultSet rs) throws SQLException {
        return new BannedPlayer(rs.getInt("id"), UUID.fromString(rs.getString("banned_by_uuid")), UUID.fromString(rs.getString("player_uuid")), rs
                .getString("player_username"), rs.getString("banned_by_username"), rs
                .getString("banned_reason"), getDateFromId(rs.getInt("banned_date")),
                getDateFromId(rs.getInt("release_date")));
    }


    @Override
    public void moveCurrentBanToBanHistory(int banId) {
        String sqlQuery = "INSERT INTO ban_history(ban_id) VALUE (?);";

        try (Connection connection = plugin.getConnectionPoolManager().getConnection();
             PreparedStatement ps = connection.prepareStatement(sqlQuery)) {

            ps.setInt(1, banId);

            deleteFromCurrentBans(banId);

            ps.executeUpdate();

        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to move the ban to ban history!\nError message: " + e.getMessage());
        }
    }


    private void deleteFromCurrentBans(int banId) {
        String sqlQuery = "DELETE FROM current_bans WHERE ban_id = ?";

        try (Connection connection = plugin.getConnectionPoolManager().getConnection();
             PreparedStatement ps = connection.prepareStatement(sqlQuery)) {

            ps.setInt(1, banId);

            ps.executeUpdate();

        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to delete current ban!\nError message: " + e.getMessage());
        }
    }

    @Override
    public int getTotalBannedTimesForPlayer(String playerUsername) {
        String sqlQuery = "SELECT COUNT(*) AS total FROM bans WHERE player_username = ?";

        return countPunishmentTimes(playerUsername, sqlQuery);
    }

    @Override
    public void saveKick(Kick kick) {
        String sqlQuery = "INSERT INTO kicks (kicked_by_uuid, kicked_by_username, player_uuid, player_username, reason, date) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = plugin.getConnectionPoolManager().getConnection();
             PreparedStatement ps = connection.prepareStatement(sqlQuery)) {

            int kickedTime = saveDateAndGetId(kick.getDate());

            ps.setString(1, kick.getKickedByUuid().toString());
            ps.setString(2, kick.getKickedByUsername());
            ps.setString(3, kick.getKickedPlayerUuid().toString());
            ps.setString(4, kick.getKickedPlayerUsername());
            ps.setString(5, kick.getReason());
            ps.setInt(6, kickedTime);

            ps.executeUpdate();

        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save kicked player!\nError message: " + e.getMessage());
        }
    }

    @Override
    public int getLastKickedId() {
        String sqlQuery = "SELECT * FROM kicks ORDER BY id DESC LIMIT 1;";

        try (Connection connection = plugin.getConnectionPoolManager().getConnection();
             PreparedStatement ps = connection.prepareStatement(sqlQuery)) {

            ResultSet rs = ps.executeQuery();

            return rs.next() ? rs.getInt("id") : -1;

        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to retrieve last kicks id!\nError message: " + e.getMessage());
        }

        return -1;
    }

    @Override
    public int getKickedTimesForPlayer(String playerName) {
        String sqlQuery = "SELECT COUNT(*) AS total FROM kicks where player_username = ?";

        return countPunishmentTimes(playerName, sqlQuery);
    }

    @Override
    public void saveTextFromChat(SpiedText spiedText) {
        String sqlQuery = "INSERT INTO spied_text (player_uuid, player_username, text, date) VALUES (?, ?, ?, ?);";

        try (Connection connection = plugin.getConnectionPoolManager().getConnection();
             PreparedStatement ps = connection.prepareStatement(sqlQuery)) {

            int spiedTime = saveDateAndGetId(spiedText.getDate());

            ps.setString(1, spiedText.getSenderUuid().toString());
            ps.setString(2, spiedText.getPlayerUsername());
            ps.setString(3, spiedText.getText());
            ps.setInt(4, spiedTime);

            ps.executeUpdate();

        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "failed to save spied text!\nError message: " + e.getMessage());
        }
    }

    @Override
    public Set<SpiedText> retrieveSpiedText(String playerUsername) {
        String sqlQuery = "SELECT * FROM spiedText WHERE player_username = ?";

        try (Connection connection = plugin.getConnectionPoolManager().getConnection();
             PreparedStatement ps = connection.prepareStatement(sqlQuery)) {

            ps.setString(1, playerUsername);

            ResultSet rs = ps.executeQuery();

            Set<SpiedText> spiedTexts = new HashSet<>();
            while (rs.next()) {
                spiedTexts.add(new SpiedText(UUID.fromString(rs.getString("player_uuid")), rs.getString("player_username"), rs.getString("text"), getDateFromId(rs.getInt("date"))));
            }

            return spiedTexts;

        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "failed to save retrieve spied text!\nError message: " + e.getMessage());
        }

        return null;
    }

    @Override
    public void saveTicket(Ticket ticket) {
        String sqlQuery = "INSERT INTO tickets (reporter_uuid, reporter_username, small_description, description, creation_date) VALUES (?, ?, ?, ?, ?);";
        try (Connection connection = plugin.getConnectionPoolManager().getConnection();
             PreparedStatement ps = connection.prepareStatement(sqlQuery)) {

            int tickedTime = saveDateAndGetId(ticket.getCreationDate());

            ps.setString(1, ticket.getReporterUuid().toString());
            ps.setString(2, ticket.getReporterUsername());
            ps.setString(3, ticket.getSmallDescription());
            ps.setString(4, ticket.getDescription());
            ps.setInt(5, tickedTime);

            ps.executeUpdate();

        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "failed to save ticket!\nError message: " + e.getMessage());
        }
    }


    @Override
    public int getAmountOfTickets(String playerUsername) {
        String sqlQuery = "SELECT COUNT(*) AS total FROM tickets where reporter_username = ?;";

        try (Connection connection = plugin.getConnectionPoolManager().getConnection();
             PreparedStatement ps = connection.prepareStatement(sqlQuery)) {

            ps.setString(1, playerUsername);

            ResultSet rs = ps.executeQuery();

            return rs.next() ? rs.getInt("total") : 0;

        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Couldn't retrieve the total amount of tickets for player!\nError message: " + e.getMessage());
        }

        return 0;
    }

    @Override
    public Set<Integer> retrieveTicketsIdForPlayer(String playerUsername) {
        String sqlQuery = "SELECT * FROM tickets WHERE reporter_username = ?";

        try (Connection connection = plugin.getConnectionPoolManager().getConnection();
             PreparedStatement ps = connection.prepareStatement(sqlQuery)) {

            ps.setString(1, playerUsername);

            ResultSet rs = ps.executeQuery();

            Set<Integer> ids = new HashSet<>();

            while (rs.next()) {
                ids.add(rs.getInt("id"));
            }

            return ids;


        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to retrieve tickets id!\nError message: " + e.getMessage());
        }
        return null;
    }


    @Override
    public Ticket retrieveTicket(int id, String playerUsername) {
        if (verifyTicketId(id, playerUsername)) {
            String sqlQuery = "SELECT * FROM tickets WHERE tickets.id = ?";

            try (Connection connection = plugin.getConnectionPoolManager().getConnection();
                 PreparedStatement ps = connection.prepareStatement(sqlQuery)) {

                ps.setInt(1, id);

                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    return new Ticket(rs.getInt("id"), UUID.fromString(rs.getString("reporter_uuid")), rs.getString("reporter_username"), rs.getString("small_description"), rs.getString("description"), getDateFromId(rs.getInt("creation_date")));
                }

            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to retrieve ticket!\nError message: " + e.getMessage());
            }
        }
        return null;
    }

    @Override
    public int getLastTicketId() {
        String sqlQuery = "SELECT * FROM tickets ORDER BY id DESC LIMIT 1;";

        try (Connection connection = plugin.getConnectionPoolManager().getConnection();
             PreparedStatement ps = connection.prepareStatement(sqlQuery)) {

            ResultSet rs = ps.executeQuery();

            return rs.next() ? rs.getInt("id") : -1;

        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to retrieve last ticket id!\nError message: " + e.getMessage());
        }

        return -1;

    }

    @Override
    public boolean verifyTicketId(int id, String playerUsername) {
        String sqlQuery = "SELECT * FROM tickets WHERE id = ? and reporter_username = ?";

        try (Connection connection = plugin.getConnectionPoolManager().getConnection();
             PreparedStatement ps = connection.prepareStatement(sqlQuery)) {

            ps.setInt(1, id);
            ps.setString(2, playerUsername);

            ResultSet rs = ps.executeQuery();

            return rs.next();

        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to verify ticket id!\nError message: " + e.getMessage());
        }
        return false;
    }

    @Override
    public void saveMute(MutedPlayer mutedPlayer) {
        String sqlQuery = "INSERT INTO mutes (muted_by_uuid, muted_by_username, player_uuid, player_username, channel_name, muted_date, release_date) VALUES (?, ?, ?, ?, ?, ?, ?);";

        try (Connection connection = plugin.getConnectionPoolManager().getConnection();
             PreparedStatement ps = connection.prepareStatement(sqlQuery, Statement.RETURN_GENERATED_KEYS)) {

            int mutedDate = saveDateAndGetId(mutedPlayer.getMutedDate());
            int releaseDate = saveDateAndGetId(mutedPlayer.getReleaseDate());

            ps.setString(1, mutedPlayer.getMutedByUuid().toString());
            ps.setString(2, mutedPlayer.getMutedByUsername());
            ps.setString(3, mutedPlayer.getMutedPlayerUuid().toString());
            ps.setString(4, mutedPlayer.getMutedByUsername());
            ps.setString(5, mutedPlayer.getChannelName());
            ps.setInt(6, mutedDate);
            ps.setInt(7, releaseDate);

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();

            if (rs.next()) {
                saveToCurrentMute(rs.getInt(1));
            }

        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save mute!\nError message: " + e.getMessage());
        }
    }

    private void saveToCurrentMute(int id) {
        String sqlQuery = "INSERT INTO current_mutes (mute_id) VALUE (?)";

        try (Connection connection = plugin.getConnectionPoolManager().getConnection();
             PreparedStatement ps = connection.prepareStatement(sqlQuery, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save mute id to current mutes!\nError message: " + e.getMessage());
        }
    }

    @Override
    public void removeMute(String playerName, String channelName) {
        System.out.println(0);
        String sqlQuery = "SELECT * FROM current_mutes LEFT JOIN mutes m on current_mutes.mute_id = m.id WHERE player_username = ? and channel_name = ?";
        try (Connection connection = plugin.getConnectionPoolManager().getConnection();
             PreparedStatement ps = connection.prepareStatement(sqlQuery)) {
            System.out.println(1);

            ps.setString(1, playerName);
            ps.setString(2, channelName);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                System.out.println(2);
                moveCurrentMuteToMuteHistory(rs.getInt("m.id"));
                deleteCurrentMute(rs.getInt("m.id"));
            }

        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to remove mute!\nError message: " + e.getMessage());
        }
    }

    private void moveCurrentMuteToMuteHistory(int id) {
        String sqlQuery = "INSERT INTO mute_history (mute_id) VALUES (?)";

        try (Connection connection = plugin.getConnectionPoolManager().getConnection();
             PreparedStatement ps = connection.prepareStatement(sqlQuery, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, id);

            ps.executeUpdate();

        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to move mute to mute history!\nError message: " + e.getMessage());
        }
    }

    private void deleteCurrentMute(int id) {
        System.out.println(3);
        String sqlQuery = "DELETE FROM current_mutes WHERE id = ?";

        try (Connection connection = plugin.getConnectionPoolManager().getConnection();
             PreparedStatement ps = connection.prepareStatement(sqlQuery, Statement.RETURN_GENERATED_KEYS)) {

            System.out.println(4);
            ps.setInt(1, id);

            ps.executeUpdate();

        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to delete mute from current mutes!\nError message: " + e.getMessage());
        }
    }

    @Override
    public boolean isPlayerMutedByUuid(UUID playerUuid, String channelName) {
        String sqlQuery = "SELECT * FROM current_mutes LEFT JOIN mutes m on m.id = current_mutes.mute_id WHERE player_uuid = ? and channel_name = ?";

        try (Connection connection = plugin.getConnectionPoolManager().getConnection();
             PreparedStatement ps = connection.prepareStatement(sqlQuery)) {

            ps.setString(1, playerUuid.toString());
            ps.setString(2, channelName);

            ResultSet rs = ps.executeQuery();

            return rs.next();

        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to check if player is muted!\nError message: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean isPlayerMutedByUsername(String playerUsername, String channelName) {
        String sqlQuery = "SELECT * FROM current_mutes LEFT JOIN mutes m on m.id = current_mutes.mute_id WHERE player_username = ? and channel_name = ?";

        try (Connection connection = plugin.getConnectionPoolManager().getConnection();
             PreparedStatement ps = connection.prepareStatement(sqlQuery)) {

            ps.setString(1, playerUsername);
            ps.setString(2, channelName);

            ResultSet rs = ps.executeQuery();

            return rs.next();

        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to check if player is muted!\nError message: " + e.getMessage());
        }
        return false;

    }

    @Override
    public MutedPlayer loadMutedPlayer(String playerUsername, String channelName) {
        String sqlQuery = "SELECT * FROM current_mutes LEFT JOIN mutes m on m.id = current_mutes.mute_id WHERE player_username = ? and channel_name = ?";

        try (Connection connection = plugin.getConnectionPoolManager().getConnection();
             PreparedStatement ps = connection.prepareStatement(sqlQuery)) {

            ps.setString(1, playerUsername);
            ps.setString(2, channelName);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return convertRsToMutedPlayer(rs);
            }

        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to acquire muted player!\nError message: " + e.getMessage());
        }
        return null;
    }

    private MutedPlayer convertRsToMutedPlayer(ResultSet rs) throws SQLException {
        return new MutedPlayer(rs.getInt("id"), UUID.fromString(rs.getString("muted_by_uuid")), UUID.fromString(rs.getString("player_uuid")), rs
                .getString("muted_by_username"), rs.getString("player_username"), rs.getString("channel_name"),
                getDateFromId(rs.getInt("muted_date")), getDateFromId(rs.getInt("release_date")));
    }


    @Override
    public int getTotalMutedTimesForPlayer(String playerUsername) {
        String sqlQuery = "SELECT COUNT(*) AS total FROM mutes WHERE player_username = ?";

        return countPunishmentTimes(playerUsername, sqlQuery);

    }

    private int countPunishmentTimes(String playerUsername, String sqlQuery) {
        try (Connection connection = plugin.getConnectionPoolManager().getConnection();
             PreparedStatement ps = connection.prepareStatement(sqlQuery)) {

            ps.setString(1, playerUsername);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("total");
            } else {
                return 0;
            }

        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Couldn't count the total banned times!\nError message: " + e.getMessage());
        }
        return 0;
    }


    @Override
    public int getLastMuteId() {
        String sqlQuery = "SELECT * FROM mutes ORDER BY id DESC LIMIT 1;";

        try (Connection connection = plugin.getConnectionPoolManager().getConnection();
             PreparedStatement ps = connection.prepareStatement(sqlQuery)) {

            ResultSet rs = ps.executeQuery();

            return rs.next() ? rs.getInt("id") : -1;

        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to retrieve last mutes id!\nError message: " + e.getMessage());
        }

        return -1;
    }

    private int saveDateAndGetId(LocalDateTime localDateTime) {
        String sqlQuery = "INSERT INTO timeDate (DATE) VALUE (?);";

        try (Connection connection = plugin.getConnectionPoolManager().getConnection();
             PreparedStatement ps = connection.prepareStatement(sqlQuery, PreparedStatement.RETURN_GENERATED_KEYS)) {

            ps.setTimestamp(1, Timestamp.valueOf(localDateTime));

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();

            return rs.next() ? rs.getInt(1) : -1;

        } catch (SQLException e) {
            this.plugin.getLogger().log(Level.WARNING, "Failed to save date time!\nError message: " + e.getMessage());
        }

        return -1;
    }

    private LocalDateTime getDateFromId(int id) {
        String sqlQuery = "SELECT * FROM timeDate WHERE ID = ?";

        try (Connection connection = plugin.getConnectionPoolManager().getConnection();
             PreparedStatement ps = connection.prepareStatement(sqlQuery)) {

            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();

            if (rs != null) {
                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME;

                return rs.getTimestamp("DATE").toLocalDateTime();
            }

        } catch (SQLException e) {
            this.plugin.getLogger().log(Level.WARNING, "Failed to get date time!\nError message: " + e.getMessage());
        }
        return LocalDateTime.now();
    }
}

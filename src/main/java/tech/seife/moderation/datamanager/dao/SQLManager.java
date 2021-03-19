package tech.seife.moderation.datamanager.dao;

import org.bukkit.Bukkit;
import tech.seife.moderation.Moderation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SQLManager {

    private final ConnectionPoolManager connectionPoolManager;
    private final Moderation plugin;

    public SQLManager(Moderation plugin, ConnectionPoolManager connectionPoolManager) {
        this.plugin = plugin;
        this.connectionPoolManager = connectionPoolManager;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            createBansTable();
            createBanHistoryTable();
            createCurrentBansTable();

            createMuteTable();
            createMuteHistoryTable();
            createCurrentMutesTable();

            createSpiedTextTable();

            createKickTable();

            createTicketsTable();
        });
    }

    private void createBansTable() {
        String sqlQuery = "CREATE TABLE IF NOT EXISTS bans" +
                "(" +
                "    id                 INT           NOT NULL AUTO_INCREMENT," +
                "    banned_by_uuid     VARCHAR(36)   NOT NULL," +
                "    banned_by_username VARCHAR(500)  NOT NULL," +
                "    player_uuid        VARCHAR(36)   NOT NULL," +
                "    player_username    VARCHAR(500)  NOT NULL," +
                "    banned_reason      VARCHAR(2000) NOT NULL," +
                "    banned_date        TIMESTAMP(6)   NOT NULL," +
                "    release_date       TIMESTAMP(6)   NOT NULL," +
                "    PRIMARY KEY (id)" +
                ");";
        createTable(sqlQuery);
    }

    private void createBanHistoryTable() {
        String sqlQuery = "CREATE TABLE IF NOT EXISTS ban_history" +
                "(" +
                "    id     INT NOT NULL AUTO_INCREMENT," +
                "    ban_id INT NOT NULL," +
                "    FOREIGN KEY (ban_id) REFERENCES bans (id)," +
                "    PRIMARY KEY (id)" +
                ");";

        createTable(sqlQuery);
    }

    private void createKickTable() {
        String sqlQuery = "CREATE TABLE IF NOT EXISTS kicks" +
                "(" +
                "    id                 INT          NOT NULL AUTO_INCREMENT," +
                "    kicked_by_uuid     VARCHAR(36)  NOT NULL," +
                "    kicked_by_username VARCHAR(500) NOT NULL," +
                "    player_uuid        VARCHAR(36)  NOT NULL," +
                "    player_username    VARCHAR(500) NOT NULL," +
                "    reason             VARCHAR(200) NOT NULL," +
                "    date               TIMESTAMP(6)  NOT NULL DEFAULT (CURRENT_DATE)," +
                "    PRIMARY KEY (id)" +
                ");";

        createTable(sqlQuery);
    }

    private void createCurrentBansTable() {
        String sqlQuery = "CREATE TABLE IF NOT EXISTS current_bans" +
                "(" +
                "    id     INT NOT NULL AUTO_INCREMENT," +
                "    ban_id INT NOT NULL," +
                "    FOREIGN KEY (ban_id) REFERENCES bans (id)," +
                "    PRIMARY KEY (id)" +
                ");";
        createTable(sqlQuery);
    }

    private void createTicketsTable() {
        String sqlQuery = "CREATE TABLE IF NOT EXISTS tickets" +
                "(" +
                "    id                INT          NOT NULL AUTO_INCREMENT," +
                "    reporter_uuid     VARCHAR(36)  NOT NULL," +
                "    reporter_username VARCHAR(500) NOT NULL," +
                "    small_description VARCHAR(500) NOT NULL," +
                "    description       TEXT         NOT NULL," +
                "    creation_date     TIMESTAMP(6)  NOT NULL," +
                "    PRIMARY KEY (id)" +
                ");";

        createTable(sqlQuery);
    }

    private void createMuteTable() {
        String sqlQuery = "CREATE TABLE IF NOT EXISTS mutes" +
                "(" +
                "    id                INT          NOT NULL AUTO_INCREMENT," +
                "    muted_by_uuid     VARCHAR(36)  NOT NULL," +
                "    muted_by_username VARCHAR(500) NOT NULL," +
                "    player_uuid       VARCHAR(36)  NOT NULL," +
                "    player_username   VARCHAR(500) NOT NULL," +
                "    channel_name      VARCHAR(500) NOT NULL," +
                "    muted_date        TIMESTAMP(6)  NOT NULL," +
                "    release_date      TIMESTAMP(6)  NOT NULL," +
                "    PRIMARY KEY (id)" +
                ");";

        createTable(sqlQuery);
    }

    private void createMuteHistoryTable() {
        String sqlQuery = "CREATE TABLE IF NOT EXISTS mute_history" +
                "(id      INT NOT NULL AUTO_INCREMENT," +
                "    mute_id INT NOT NULL," +
                "    FOREIGN KEY (mute_id) REFERENCES mutes (id)," +
                "    PRIMARY KEY (id)" +
                ");";

        createTable(sqlQuery);
    }

    private void createCurrentMutesTable() {
        String sqlQuery = "CREATE TABLE IF NOT EXISTS current_mutes" +
                "(id     INT NOT NULL AUTO_INCREMENT," +
                "    mute_id INT NOT NULL," +
                "    FOREIGN KEY (mute_id) REFERENCES mutes (id)," +
                "    PRIMARY KEY (id)" +
                ");";

        createTable(sqlQuery);
    }

    public void createSpiedTextTable() {
        String sqlQuery = "CREATE TABLE IF NOT EXISTS spied_text" +
                "(id                 INT          NOT NULL  AUTO_INCREMENT," +
                "    player_uuid     VARCHAR(36)  NOT NULL," +
                "    player_username VARCHAR(500)  NOT NULL," +
                "    text            VARCHAR(2000) NOT NULL, " +
                "    date            date          NOT NULL," +
                "    PRIMARY KEY (id)" +
                ");";

        createTable(sqlQuery);
    }

    private void createTable(String sqlQuery) {
        try (Connection connection = connectionPoolManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sqlQuery)) {
            ps.execute();
        } catch (SQLException e) {
            plugin.getLogger().warning(e.getMessage());
        }
    }
}

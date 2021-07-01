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
            createDateTable();
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

    private void createDateTable() {
        String sqlQuery = "CREATE TABLE IF NOT EXISTS timeDate(" +
                "  ID INT NOT NULL AUTO_INCREMENT," +
                "  DATE TIMESTAMP(6) NOT NULL," +
                "  PRIMARY KEY (ID)" +
                ")";
        createTable(sqlQuery);
    }

    private void createBansTable() {
        String sqlQuery = "CREATE TABLE IF NOT EXISTS bans (" +
                "  id INT NOT NULL AUTO_INCREMENT," +
                "  banned_by_uuid VARCHAR(36) NOT NULL," +
                "  banned_by_username VARCHAR(500) NOT NULL," +
                "  player_uuid VARCHAR(36) NOT NULL," +
                "  player_username VARCHAR(500) NOT NULL," +
                "  banned_reason VARCHAR(2000) NOT NULL," +
                "  banned_date INT NOT NULL," +
                "  release_date INT NOT NULL," +
                "  FOREIGN KEY (banned_date) REFERENCES timeDate (ID)," +
                "  FOREIGN KEY (release_date) REFERENCES timeDate (ID)," +
                "  PRIMARY KEY (id)" +
                ");";
        createTable(sqlQuery);
    }

    private void createBanHistoryTable() {
        String sqlQuery = "CREATE TABLE IF NOT EXISTS ban_history(" +
                "  id INT NOT NULL AUTO_INCREMENT," +
                "  ban_id INT NOT NULL," +
                "  FOREIGN KEY (ban_id) REFERENCES bans (id)," +
                "  PRIMARY KEY (id)" +
                ");";
        createTable(sqlQuery);
    }

    private void createKickTable() {
        String sqlQuery = "CREATE TABLE IF NOT EXISTS kicks(" +
                "  id INT NOT NULL AUTO_INCREMENT," +
                "  kicked_by_uuid VARCHAR(36) NOT NULL," +
                "  kicked_by_username VARCHAR(500) NOT NULL," +
                "  player_uuid VARCHAR(36) NOT NULL," +
                "  player_username VARCHAR(500) NOT NULL," +
                "  reason VARCHAR(200) NOT NULL," +
                "  date INT NOT NULL," +
                "  foreign key (date) references timeDate (ID)," +
                "  PRIMARY KEY (id)" +
                ");";
        createTable(sqlQuery);
    }

    private void createCurrentBansTable() {
        String sqlQuery = "CREATE TABLE IF NOT EXISTS current_bans(" +
                "  id INT NOT NULL AUTO_INCREMENT," +
                "  ban_id INT NOT NULL," +
                "  FOREIGN KEY (ban_id) REFERENCES bans (id)," +
                "  PRIMARY KEY (id)" +
                ");";
        createTable(sqlQuery);
    }

    private void createTicketsTable() {
        String sqlQuery = "CREATE TABLE IF NOT EXISTS tickets(" +
                "  id INT NOT NULL AUTO_INCREMENT," +
                "  reporter_uuid VARCHAR(36) NOT NULL," +
                "  reporter_username VARCHAR(500) NOT NULL," +
                "  small_description VARCHAR(500) NOT NULL," +
                "  description TEXT NOT NULL," +
                "  creation_date INT NOT NULL," +
                "  FOREIGN KEY (creation_date) REFERENCES timeDate (ID)," +
                "  PRIMARY KEY (id)" +
                ");";
        createTable(sqlQuery);
    }

    private void createMuteTable() {
        String sqlQuery = "CREATE TABLE IF NOT EXISTS mutes(" +
                "  id INT NOT NULL AUTO_INCREMENT," +
                "  muted_by_uuid VARCHAR(36) NOT NULL," +
                "  muted_by_username VARCHAR(500) NOT NULL," +
                "  player_uuid VARCHAR(36) NOT NULL," +
                "  player_username VARCHAR(500) NOT NULL," +
                "  channel_name VARCHAR(500) NOT NULL," +
                "  muted_date INT NOT NULL," +
                "  release_date INT NOT NULL," +
                "  foreign key (muted_date) REFERENCES timeDate (id)," +
                "  foreign key (release_date) REFERENCES timeDate(id)," +
                "  PRIMARY KEY (id)" +
                ");";
        createTable(sqlQuery);
    }

    private void createMuteHistoryTable() {
        String sqlQuery = "CREATE TABLE IF NOT EXISTS mute_history(" +
                "  id INT NOT NULL AUTO_INCREMENT," +
                "  mute_id INT NOT NULL," +
                "  FOREIGN KEY (mute_id) REFERENCES mutes (id)," +
                "  PRIMARY KEY (id)" +
                ");";
        createTable(sqlQuery);
    }

    private void createCurrentMutesTable() {
        String sqlQuery = "CREATE TABLE IF NOT EXISTS current_mutes(" +
                "  id INT NOT NULL AUTO_INCREMENT," +
                "  mute_id INT NOT NULL," +
                "  FOREIGN KEY (mute_id) REFERENCES mutes (id)," +
                "  PRIMARY KEY (id)" +
                ");";
        createTable(sqlQuery);
    }

    public void createSpiedTextTable() {
        String sqlQuery = "CREATE TABLE IF NOT EXISTS spied_text(" +
                "  id INT NOT NULL AUTO_INCREMENT," +
                "  player_uuid VARCHAR(36) NOT NULL," +
                "  player_username VARCHAR(500) NOT NULL," +
                "  text VARCHAR(2000) NOT NULL," +
                "  date INT NOT NULL," +
                "  foreign key (date) REFERENCES timeDate (ID)," +
                "  PRIMARY KEY (id)" +
                ");";
        createTable(sqlQuery);
    }

    private void createTable(String sqlQuery) {
        try {
            Connection connection = this.connectionPoolManager.getConnection();
            try {
                PreparedStatement ps = connection.prepareStatement(sqlQuery);
                try {
                    ps.execute();
                    if (ps != null)
                        ps.close();
                } catch (Throwable throwable) {
                    if (ps != null)
                        try {
                            ps.close();
                        } catch (Throwable throwable1) {
                            throwable.addSuppressed(throwable1);
                        }
                    throw throwable;
                }
                if (connection != null)
                    connection.close();
            } catch (Throwable throwable) {
                if (connection != null)
                    try {
                        connection.close();
                    } catch (Throwable throwable1) {
                        throwable.addSuppressed(throwable1);
                    }
                throw throwable;
            }
        } catch (SQLException e) {
            this.plugin.getLogger().warning(e.getMessage());
        }
    }
}

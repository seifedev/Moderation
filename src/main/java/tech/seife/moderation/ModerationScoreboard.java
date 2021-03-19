package tech.seife.moderation;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

public class ModerationScoreboard {

    private final Moderation plugin;

    public ModerationScoreboard(Moderation plugin) {
        this.plugin = plugin;
    }

    public void setScoreboard(Player scoreboardWielder, Player playerToInspect) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

        Objective objective = scoreboard.registerNewObjective("Moderation", "dummy", "Moderation Scoreboard");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> addScores(objective, playerToInspect));


        scoreboardWielder.setScoreboard(scoreboard);
    }

    private void addScores(Objective objective, Player player) {
        Score moderatedPlayer = objective.getScore(ChatColor.DARK_PURPLE + "Player name: >> " + player.getName() + " <<");
        moderatedPlayer.setScore(15);

        Score kickedTimes = objective.getScore(ChatColor.DARK_BLUE + "Times got kicked: >> " + plugin.getDataManager().getKickedTimesForPlayer(player.getName()) + " <<");
        kickedTimes.setScore(14);

        Score ticketsApplied = objective.getScore(ChatColor.GREEN + "Times applied a ticket: >> " + plugin.getDataManager().getAmountOfTickets(player.getName()) + " <<");
        ticketsApplied.setScore(13);

        Score bannedTimes = objective.getScore(ChatColor.RED + "Times banned: >> " + plugin.getDataManager().getTotalBannedTimesForPlayer(player.getName()) + " <<");
        bannedTimes.setScore(12);

        Score mutedTimes = objective.getScore(ChatColor.YELLOW + "Times muted: >> " + plugin.getDataManager().getTotalMutedTimesForPlayer(player.getName()) + " <<");
        mutedTimes.setScore(11);
    }
}

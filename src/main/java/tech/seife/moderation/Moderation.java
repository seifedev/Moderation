package tech.seife.moderation;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import tech.seife.chatutilities.ChatUtilities;
import tech.seife.moderation.commands.*;
import tech.seife.moderation.commands.bans.BanPlayer;
import tech.seife.moderation.commands.bans.RemoveBan;
import tech.seife.moderation.commands.bans.ViewPlayerBanHistory;
import tech.seife.moderation.commands.mutes.MutePlayer;
import tech.seife.moderation.commands.mutes.UnMutePlayer;
import tech.seife.moderation.commands.spy.*;
import tech.seife.moderation.commands.support.TicketApply;
import tech.seife.moderation.commands.support.TicketInfo;
import tech.seife.moderation.commands.support.ViewTickets;
import tech.seife.moderation.datamanager.banned.BannedPlayerManager;
import tech.seife.moderation.datamanager.dao.*;
import tech.seife.moderation.datamanager.kicks.KickManager;
import tech.seife.moderation.datamanager.mutes.MutedPlayerManager;
import tech.seife.moderation.datamanager.spiedtext.SpiedTextManager;
import tech.seife.moderation.datamanager.tickets.TicketManager;
import tech.seife.moderation.events.*;
import tech.seife.moderation.packets.HidePlayer;
import tech.seife.moderation.utils.MessageManager;

public class Moderation extends JavaPlugin {

    private BannedPlayerManager bannedPlayerManager;

    private DataManager dataManager;
    private CustomFiles customFiles;
    private ConnectionPoolManager connectionPoolManager;
    private CachedData cachedData;
    private TicketManager ticketManager;
    private KickManager kickManager;
    private ProtocolManager protocolManager;
    private ChatUtilities chatUtilities;
    private MutedPlayerManager mutedPlayerManager;
    private SpiedTextManager spiedTextManager;

    @Override
    public void onEnable() {
        initialize();
        registerCommands();
        registerEvents();
        listenPackets();
    }

    private void listenPackets() {
        HidePlayer hidePlayer = new HidePlayer(this, protocolManager, cachedData);
        hidePlayer.hidePlayer();
    }

    private void initialize() {
        saveDefaultConfig();

        protocolManager = ProtocolLibrary.getProtocolManager();

        customFiles = new CustomFiles(this);
        cachedData = new CachedData();
        connectionPoolManager = new ConnectionPoolManager(getConfig());

        new SQLManager(this, connectionPoolManager);

        dataManager = new DataHandler(this).getDataManager();

        ticketManager = new TicketManager(dataManager);
        bannedPlayerManager = new BannedPlayerManager(this);
        kickManager = new KickManager(dataManager);

        spiedTextManager = new SpiedTextManager(dataManager);

        registerChatUtilities();


    }

    private void registerChatUtilities() {
        if (getServer().getPluginManager().isPluginEnabled("ChatUtilities")) {
            chatUtilities = (ChatUtilities) Bukkit.getPluginManager().getPlugin("ChatUtilities");

            mutedPlayerManager = new MutedPlayerManager(this);
        }
    }

    private void registerCommands() {
        getCommand("ban").setExecutor(new BanPlayer(this));
        getCommand("kick").setExecutor(new KickPlayer(this, dataManager));
        getCommand("viewInventory").setExecutor(new ViewNormaInventory());
        getCommand("viewEnderChest").setExecutor(new ViewEnderChest());
        getCommand("readBook").setExecutor(new ReadBook(this, cachedData));
        getCommand("removeBan").setExecutor(new RemoveBan(this, dataManager));
        getCommand("vanish").setExecutor(new Vanish(this));
        getCommand("enableSpy").setExecutor(new EnableSpyCommand(this, cachedData));
        getCommand("disableSpy").setExecutor(new DisableSpyCommand(this, cachedData));
        getCommand("help").setExecutor(new TicketInfo());
        getCommand("helpme").setExecutor(new TicketApply(dataManager, ticketManager));
        getCommand("helphistory").setExecutor(new ViewTickets(this));
        getCommand("viewBansHistory").setExecutor(new ViewPlayerBanHistory(this));
        getCommand("InspectPlayer").setExecutor(new InspectPlayer(this));
        getCommand("cancelInspection").setExecutor(new CancelInspectionMode());
        getCommand("setRules").setExecutor(new SetRules(this));
        getCommand("rules").setExecutor(new Rules(this));
        getCommand("who").setExecutor(new Who(this));

        if (chatUtilities != null) {
            getCommand("mute").setExecutor(new MutePlayer(this));
            getCommand("unmute").setExecutor(new UnMutePlayer(this, dataManager, chatUtilities.getChannelManager()));
        }
    }

    private void registerEvents() {
        Bukkit.getPluginManager().registerEvents(new OnPlayerJoinEvent(this), this);
        Bukkit.getPluginManager().registerEvents(new OnSignChangeEvent(cachedData), this);
        Bukkit.getPluginManager().registerEvents(new OnPlayerQuitEvent(cachedData), this);
        Bukkit.getPluginManager().registerEvents(new OnPlayerEditBookEvent(cachedData), this);
        Bukkit.getPluginManager().registerEvents(new OnPlayerCommandPreprocessEvent(this), this);
        Bukkit.getPluginManager().registerEvents(new OnInventoryClickEvent(this), this);
        Bukkit.getPluginManager().registerEvents(new OnAsyncPlayerChatEvent(this, chatUtilities.getChannelManager()), this);
    }

    @Override
    public void onDisable() {

    }

    public BannedPlayerManager getBannedPlayerManager() {
        return bannedPlayerManager;
    }

    public ConnectionPoolManager getConnectionPoolManager() {
        return connectionPoolManager;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public CachedData getCachedData() {
        return cachedData;
    }

    public CustomFiles getCustomFiles() {
        return customFiles;
    }

    public TicketManager getTicketManager() {
        return ticketManager;
    }

    public KickManager getKickManager() {
        return kickManager;
    }

    public ProtocolManager getProtocolManager() {
        return protocolManager;
    }

    public MutedPlayerManager getMutedPlayerManager() {
        return mutedPlayerManager;
    }

    public ChatUtilities getChatUtilities() {
        return chatUtilities;
    }

    public SpiedTextManager getSpiedTextManager() {
        return spiedTextManager;
    }
}

package tech.seife.moderation.packets;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import tech.seife.moderation.Moderation;
import tech.seife.moderation.datamanager.dao.CachedData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class RemoveFromTab {

    private final ProtocolManager protocolManager;
    private final CachedData cachedData;
    private final Moderation plugin;

    public RemoveFromTab(Moderation plugin, ProtocolManager protocolManager, CachedData cachedData) {
        this.plugin = plugin;
        this.protocolManager = protocolManager;
        this.cachedData = cachedData;
    }


    public void sendPackets(Player playerToChangeStatus, EnumWrappers.PlayerInfoAction playerInfoAction) {
        PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.PLAYER_INFO);

        packet.getPlayerInfoAction().write(0, playerInfoAction);

        List<PlayerInfoData> playerInfoDataList = new ArrayList<>();

        playerInfoDataList.add(new PlayerInfoData(WrappedGameProfile.fromPlayer(playerToChangeStatus), 10, EnumWrappers.NativeGameMode.fromBukkit(playerToChangeStatus.getGameMode()), WrappedChatComponent.fromText(playerToChangeStatus.getName())));

        packet.getPlayerInfoDataLists().write(0, playerInfoDataList);

        try {
            for (Player player : Bukkit.getOnlinePlayers()) {
                protocolManager.sendServerPacket(player, packet);
            }
        } catch (InvocationTargetException e) {
            plugin.getLogger().log(Level.WARNING, e.getMessage());
        }
    }
}
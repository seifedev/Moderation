package tech.seife.moderation.packets;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import tech.seife.moderation.Moderation;
import tech.seife.moderation.datamanager.dao.CachedData;

import static com.comphenix.protocol.PacketType.Play.Server.*;

public class HidePlayer {

    private static final PacketType[] ENTITY_PACKETS = {
            ENTITY_EQUIPMENT, ANIMATION, NAMED_ENTITY_SPAWN, NAMED_SOUND_EFFECT, CUSTOM_SOUND_EFFECT,
            ENTITY_SOUND, COLLECT, SPAWN_ENTITY, SPAWN_ENTITY_LIVING, SPAWN_ENTITY_PAINTING, SPAWN_ENTITY_EXPERIENCE_ORB,
            ENTITY_LOOK, BLOCK_ACTION,
            ENTITY_TELEPORT, ENTITY_HEAD_ROTATION, ENTITY_STATUS, ATTACH_ENTITY, ENTITY_METADATA,
            ENTITY_EFFECT, REMOVE_ENTITY_EFFECT, BLOCK_BREAK_ANIMATION
    };


    private final ProtocolManager protocolManager;
    private final CachedData cachedData;
    private final Moderation plugin;

    public HidePlayer(Moderation plugin, ProtocolManager protocolManager, CachedData cachedData) {
        this.plugin = plugin;
        this.protocolManager = protocolManager;
        this.cachedData = cachedData;
    }


    public void hidePlayer() {
        protocolManager.addPacketListener(constructProtocol());
    }

    private PacketAdapter constructProtocol() {
        return new PacketAdapter(plugin, ENTITY_PACKETS) {
            @Override
            public void onPacketSending(PacketEvent event) {
                if (cachedData.getOnVanishModePlayers().contains(event.getPlayer().getUniqueId())) {
                    event.setCancelled(true);
                }
            }
        };
    }
}

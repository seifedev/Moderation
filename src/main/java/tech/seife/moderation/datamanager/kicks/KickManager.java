package tech.seife.moderation.datamanager.kicks;

import tech.seife.moderation.datamanager.dao.DataManager;

import java.time.LocalDateTime;
import java.util.UUID;

public class KickManager {

    private final DataManager dataManager;

    public KickManager(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    public void addKick(UUID kickedPlayerUuid, UUID kickedByUuid, String kickedPlayerUsername, String kickedByUsername, String reason, LocalDateTime date) {
        int kickId = generateId();

        Kick kick = new Kick(kickId, kickedPlayerUuid, kickedByUuid, kickedPlayerUsername, kickedByUsername, reason, date);

        dataManager.saveKick(kick);
    }

    private int generateId() {
        return dataManager.getLastKickedId() + 1;
    }

}

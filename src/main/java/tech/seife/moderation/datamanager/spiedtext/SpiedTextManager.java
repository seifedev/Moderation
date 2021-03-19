package tech.seife.moderation.datamanager.spiedtext;

import tech.seife.moderation.datamanager.dao.DataManager;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public class SpiedTextManager {

    private final DataManager dataManager;

    public SpiedTextManager(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    public void saveSpiedText(UUID senderUuid, String playerUsername, String text) {
        dataManager.saveTextFromChat(new SpiedText(senderUuid, playerUsername, text, LocalDateTime.now()));
    }

    public Set<SpiedText> retrieveSpiedText(String playerUsername) {
        return dataManager.retrieveSpiedText(playerUsername);
    }
}

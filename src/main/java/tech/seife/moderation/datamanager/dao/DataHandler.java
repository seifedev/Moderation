package tech.seife.moderation.datamanager.dao;

import tech.seife.moderation.Moderation;

public class DataHandler {

    private final DataManager dataManager;

    public DataHandler(Moderation plugin) {
        if (plugin.getConfig().getBoolean("useDatabase")) {
            dataManager = new DataManagerDatabase(plugin);
        } else {
            dataManager = new DataManagerFiles(plugin);
        }
    }

    public DataManager getDataManager() {
        return dataManager;
    }
}

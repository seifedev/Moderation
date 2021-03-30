package tech.seife.moderation.datamanager.dao;

import tech.seife.moderation.Moderation;

public class DataHandler {

    private DataManager dataManager;
    private CustomFiles customFiles;

    public DataHandler(Moderation plugin) {
        customFiles = new CustomFiles(plugin);

        if (plugin.getConfig().getBoolean("useDatabase")) {
            dataManager = new DataManagerDatabase(plugin);
        } else {
            dataManager = new DataManagerFiles(plugin);
            customFiles.createGson(plugin);
        }
    }

    public DataManager getDataManager() {
        return dataManager;
    }
}

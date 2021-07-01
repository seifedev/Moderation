package tech.seife.moderation.datamanager.dao;

import tech.seife.moderation.Moderation;

public class DataHandler {

    private final DataManager dataManager;
    private final CustomFiles customFiles;
    private ConnectionPoolManager connectionPoolManager;

    public DataHandler(Moderation plugin) {
        customFiles = new CustomFiles(plugin);


        if (plugin.getConfig().getBoolean("useDatabase")) {
            dataManager = new DataManagerDatabase(plugin);

            connectionPoolManager = new ConnectionPoolManager(plugin.getConfig());
            new SQLManager(plugin, connectionPoolManager);

        } else {
            dataManager = new DataManagerFiles(customFiles, plugin.getLogger());
            customFiles.createGson(plugin);
        }
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public ConnectionPoolManager getConnectionPoolManager() {
        return connectionPoolManager;
    }

    public CustomFiles getCustomFiles() {
        return customFiles;
    }
}

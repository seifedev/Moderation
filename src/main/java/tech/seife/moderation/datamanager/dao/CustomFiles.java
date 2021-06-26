package tech.seife.moderation.datamanager.dao;

import com.google.gson.Gson;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import tech.seife.moderation.Moderation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class CustomFiles {

    private final Moderation plugin;

    private File bansFile, bansHistoryFile, currentBansFile, mutesFile, mutesHistoryFile, currentMutesFile;
    private File kicksFile, spiedText, ticketsFile, translationFile;

    private FileConfiguration translationConfig;

    private Gson gson;

    public CustomFiles(Moderation plugin) {
        this.plugin = plugin;
        gson = new Gson();
        
        translationFile = new File(plugin.getDataFolder(), "translation.yml");
        createYamlFile(translationFile);
    }

    private void createYamlFile(File file) {
        file = new File(plugin.getDataFolder(), file.getName());

        if (!file.exists()) {
            file.getParentFile().mkdirs();
            plugin.saveResource(file.getName(), false);
        }

        translationConfig = new YamlConfiguration();

        try {
            translationConfig.load(translationFile);
        } catch (IOException | InvalidConfigurationException e) {
            plugin.getLogger().log(Level.WARNING, "Couldn't load translation config file!\nError message: ", e.getMessage());
        }
    }

    public Gson createGson(Moderation plugin) {

        bansFile = new File(plugin.getDataFolder(), "bans.json");
        createFile(bansFile);

        bansHistoryFile = new File(plugin.getDataFolder(), "bansHistory.json");
        createFile(bansHistoryFile);

        currentBansFile = new File(plugin.getDataFolder(), "currentBans.json");
        createFile(currentBansFile);

        mutesFile = new File(plugin.getDataFolder(), "mutes.json");
        createFile(mutesFile);

        mutesHistoryFile = new File(plugin.getDataFolder(), "mutesHistory.json");
        createFile(mutesHistoryFile);

        currentMutesFile = new File(plugin.getDataFolder(), "currentMutes.json");
        createFile(currentMutesFile);


        kicksFile = new File(plugin.getDataFolder(), "kicks.json");
        createFile(kicksFile);

        spiedText = new File(plugin.getDataFolder(), "spiedText.json");
        createFile(spiedText);

        ticketsFile = new File(plugin.getDataFolder(), "tickets.json");
        createFile(ticketsFile);
        return gson;
    }

    private void createFile(File file) {
        if (!file.exists()) {
            plugin.saveResource(file.getName(), false);
        }

    }

    public FileConfiguration getTranslationConfig() {
        return translationConfig;
    }

    public Map getBansFile() {
        return getGson(bansFile);
    }

    public Map getBansHistoryFile() {
        return getGson(bansHistoryFile);
    }

    public Map getCurrentBansFile() {
        return getGson(currentBansFile);
    }

    public Map getMutesFile() {
        return getGson(mutesFile);
    }

    public Map getMutesHistoryFile() {
        return getGson(mutesHistoryFile);
    }

    public Map getCurrentMutesFile() {
        return getGson(currentMutesFile);
    }

    public Map getKicksFile() {
        return getGson(kicksFile);
    }

    public Map getSpiedText() {
        return getGson(spiedText);
    }


    public Map getTicketsFile() {
        return getGson(ticketsFile);
    }

    public void saveBans(Map map) {
        saveJson(bansFile, map);
    }

    public void saveBansHistory(Map map) {
        saveJson(bansHistoryFile, map);
    }

    public void saveCurrentBans(Map map) {
        saveJson(currentBansFile, map);
    }

    public void saveMutes(Map map) {
        saveJson(mutesFile, map);
    }

    public void saveHistoryMutes(Map map) {
        saveJson(mutesHistoryFile, map);
    }

    public void saveCurrentMutes(Map map) {
        saveJson(currentMutesFile, map);
    }

    public void saveKicks(Map map) {
        saveJson(kicksFile, map);
    }

    public void saveSpiedText(Map map) {
        saveJson(spiedText, map);
    }

    public void saveTicketFiles(Map map) {
        saveJson(ticketsFile, map);
    }

    private void saveJson(File file, Map map) {
        String json = gson.toJson(map);
        file.delete();
        try {
            Files.write(file.toPath(), json.getBytes());
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save json!\nErrorMessage: " + e.getMessage());
        }
    }


    private HashMap getGson(File file) {
        try {
            return gson.fromJson(new FileReader(file), HashMap.class);
        } catch (FileNotFoundException e) {
            plugin.getLogger().log(Level.WARNING, file.getName() + " wasn't found");
        }
        return null;
    }

    public Gson getGson() {
        return gson;
    }
}

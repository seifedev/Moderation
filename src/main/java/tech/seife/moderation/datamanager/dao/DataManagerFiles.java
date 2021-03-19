package tech.seife.moderation.datamanager.dao;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import tech.seife.moderation.Moderation;
import tech.seife.moderation.datamanager.banned.BannedPlayer;
import tech.seife.moderation.datamanager.kicks.Kick;
import tech.seife.moderation.datamanager.mutes.MutedPlayer;
import tech.seife.moderation.datamanager.spiedtext.SpiedText;
import tech.seife.moderation.datamanager.tickets.Ticket;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class DataManagerFiles implements DataManager {

    private final Moderation plugin;
    private final Gson gson;

    public DataManagerFiles(Moderation plugin) {
        this.plugin = plugin;
        gson = plugin.getCustomFiles().getGson();
    }

    @Override
    public void saveBan(BannedPlayer bannedPlayer) {
        JsonObject jsonObject = null;
        int banId = 0;

        if (hasPlayerBeenBannedBefore(bannedPlayer.getBannedPlayerName())) {
            jsonObject = gson.fromJson(gson.toJson(plugin.getCustomFiles().getBansFile()), JsonObject.class);
        } else {
            jsonObject = gson.fromJson(gson.toJson(plugin.getCustomFiles().getBansFile()), JsonObject.class).getAsJsonObject(bannedPlayer.getBannedUuid().toString());
        }

        banId = acquireNewBanId(jsonObject);

        JsonObject bansDetails = new JsonObject();

        addBansDetails(bannedPlayer, bansDetails);

        JsonObject bansSection = new JsonObject();
        bansSection.add(bannedPlayer.getBannedDate().toString(), bansDetails);

        JsonObject bannedPlayerUuidSection = new JsonObject();
        bannedPlayerUuidSection.add(bannedPlayer.getBannedUuid().toString(), bansSection);

        jsonObject.add(bannedPlayer.getBannedUuid().toString(), bannedPlayerUuidSection);

        plugin.getCustomFiles().saveBans(gson.fromJson(jsonObject, Map.class));

        addToCurrentBans(banId);
    }

    private boolean hasPlayerBeenBannedBefore(String name) {
        JsonObject jsonObject = gson.fromJson(gson.toJson(plugin.getCustomFiles().getBansFile()), JsonObject.class);

        for (Map.Entry<String, JsonElement> banIds : jsonObject.entrySet()) {
            for (Map.Entry<String, JsonElement> banSubSection : banIds.getValue().getAsJsonObject().entrySet()) {
                for (Map.Entry<String, JsonElement> dateSubSection : banSubSection.getValue().getAsJsonObject().entrySet()) {
                    if (dateSubSection.getValue().getAsJsonObject().get("bannedUsername").equals(name)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private int acquireNewBanId(JsonObject jsonObject) {
        return jsonObject.entrySet().size() + 1;
    }

    private void addBansDetails(BannedPlayer bannedPlayer, JsonObject bansDetails) {
        bansDetails.addProperty("bannedUuid", bannedPlayer.getBannedUuid().toString());
        bansDetails.addProperty("bannedUsername", bannedPlayer.getBannedPlayerName());
        bansDetails.addProperty("bannedByUuid", bannedPlayer.getBannedUuid().toString());
        bansDetails.addProperty("bannedByName", bannedPlayer.getBannedUuid().toString());
        bansDetails.addProperty("reason", bannedPlayer.getReason());
        bansDetails.addProperty("bannedDate", bannedPlayer.getBannedDate().toString());
        bansDetails.addProperty("releaseDate", bannedPlayer.getReleaseDate().toString());
    }

    private void addToCurrentBans(int banId) {
        JsonArray jsonArray;

        if (gson.fromJson(gson.toJson(plugin.getCustomFiles().getCurrentBansFile()), JsonObject.class) == null) {
            jsonArray = new JsonArray();
        } else {
            jsonArray = gson.fromJson(gson.toJson(plugin.getCustomFiles().getCurrentBansFile()), JsonObject.class).getAsJsonArray("bans");
        }

        jsonArray.add(banId);

        plugin.getCustomFiles().saveCurrentBans(gson.fromJson(jsonArray, Map.class));
    }

    @Override
    public void removeBan(BannedPlayer bannedPlayer) {
        if (isPlayerBannedUuidCheck(bannedPlayer.getBannedByUuid())) {
            JsonObject currentBans = gson.fromJson(gson.toJson(plugin.getCustomFiles().getCurrentBansFile()), JsonObject.class);

            JsonArray ids = currentBans.getAsJsonArray("bans");

            JsonObject bans = gson.fromJson(gson.toJson(plugin.getCustomFiles().getBansFile()), JsonObject.class);

            for (JsonElement element : ids) {
                JsonObject banDetails = bans.getAsJsonObject(element.toString()).getAsJsonObject("bans");

                for (Map.Entry<String, JsonElement> detail : banDetails.entrySet()) {
                    if (detail.getKey().equals("bannedUsername") && detail.getValue().getAsString().equals(bannedPlayer.getBannedPlayerName())) {
                        currentBans.remove(element.getAsString());
                        plugin.getCustomFiles().saveCurrentMutes(gson.fromJson(currentBans, Map.class));
                        break;
                    }
                }
            }

        }
    }

    @Override
    public boolean isPlayerBannedUuidCheck(UUID playerUuid) {
        JsonObject currentBans = gson.fromJson(gson.toJson(plugin.getCustomFiles().getCurrentBansFile()), JsonObject.class);

        JsonArray ids = currentBans.getAsJsonArray("bans");

        JsonObject bans = gson.fromJson(gson.toJson(plugin.getCustomFiles().getBansFile()), JsonObject.class);

        for (JsonElement element : ids) {
            JsonObject banDetails = bans.getAsJsonObject(element.toString()).getAsJsonObject("bans");

            for (Map.Entry<String, JsonElement> detail : banDetails.entrySet()) {
                if (detail.getKey().equals("bannedUuid") && detail.getValue().getAsString().equals(playerUuid)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isPlayerBannedUsernameCheck(String playerUsername) {
        JsonObject currentBans = gson.fromJson(gson.toJson(plugin.getCustomFiles().getCurrentBansFile()), JsonObject.class);

        JsonArray ids = currentBans.getAsJsonArray("bans");

        JsonObject bans = gson.fromJson(gson.toJson(plugin.getCustomFiles().getBansFile()), JsonObject.class);

        for (JsonElement element : ids) {
            JsonObject banDetails = bans.getAsJsonObject(element.toString()).getAsJsonObject("bans");

            for (Map.Entry<String, JsonElement> detail : banDetails.entrySet()) {
                if (detail.getKey().equals("bannedUsername") && detail.getValue().getAsString().equals(playerUsername)) {
                    return true;
                }
            }
        }
        return false;
    }


    @Override
    public Set<BannedPlayer> loadPlayerBanHistory(String playerUsername) {
        JsonObject bansHistory = gson.fromJson(gson.toJson(plugin.getCustomFiles().getBansHistoryFile()), JsonObject.class);

        if (bansHistory != null) {
            JsonArray ids = bansHistory.getAsJsonArray("bans");

            JsonObject bans = gson.fromJson(gson.toJson(plugin.getCustomFiles().getBansFile()), JsonObject.class);

            Set<BannedPlayer> bannedPlayers = new HashSet<>();

            for (JsonElement element : ids) {
                JsonObject banDetails = bans.getAsJsonObject(element.toString()).getAsJsonObject("bans");
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

                for (Map.Entry<String, JsonElement> detail : banDetails.entrySet()) {
                    if (detail.getKey().equals("bannedUsername") && detail.getValue().getAsString().equals(playerUsername)) {
                        bannedPlayers.add(getBannedPlayerFromJsonObject(parseIntegerFromString(element.getAsString()), banDetails, formatter));
                    }
                }
            }
            return bannedPlayers;
        }
        return null;
    }

    @Override
    public BannedPlayer retrieveCurrentBannedPlayerInformation(String playerUsername) {
        if (isPlayerBannedUsernameCheck(playerUsername)) {
            JsonObject currentBans = gson.fromJson(gson.toJson(plugin.getCustomFiles().getCurrentBansFile()), JsonObject.class);

            JsonArray ids = currentBans.getAsJsonArray("bans");

            JsonObject bans = gson.fromJson(gson.toJson(plugin.getCustomFiles().getBansFile()), JsonObject.class);

            for (JsonElement element : ids) {
                JsonObject banDetails = bans.getAsJsonObject(element.toString()).getAsJsonObject("bans");
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

                for (Map.Entry<String, JsonElement> detail : banDetails.entrySet()) {
                    if (detail.getKey().equals("bannedUsername") && detail.getValue().getAsString().equals(playerUsername)) {
                        return getBannedPlayerFromJsonObject(parseIntegerFromString(element.getAsString()), banDetails, formatter);
                    }
                }
            }
        }
        return null;
    }

    @Override
    public int getLastBanId() {
        JsonObject bans = gson.fromJson(gson.toJson(plugin.getCustomFiles().getBansFile()), JsonObject.class);

        JsonArray ids = bans.getAsJsonArray("bans");

        return ids.size();
    }

    private BannedPlayer getBannedPlayerFromJsonObject(int id, JsonObject banDetails, DateTimeFormatter formatter) {
        return new BannedPlayer(id, UUID.fromString(banDetails.get("bannedUuid").toString()), UUID.fromString(banDetails.get("bannedByUuid").toString()), banDetails.get("bannedUsername").toString(), banDetails.get("bannedByName").toString(), banDetails.get("reason").toString(), LocalDateTime.parse(banDetails.get("bannedDate").toString(), formatter), LocalDateTime.parse(banDetails.get("bannedDate").toString(), formatter));
    }

    private int parseIntegerFromString(String number) {
        try {
            return Integer.parseInt(number);
        } catch (NumberFormatException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to parse integer!\nError message: " + e.getMessage());
            return -1;
        }
    }

    @Override
    public void moveCurrentBanToBanHistory(int banId) {
        JsonObject banHistory = gson.fromJson(gson.toJson(plugin.getCustomFiles().getBansHistoryFile()), JsonObject.class);

        JsonArray bans;
        if (banHistory.get("bans") != null) {
            bans = new JsonArray();
        } else {
            bans = banHistory.get("bans").getAsJsonArray();
        }
        bans.add(banId);

        banHistory.add("bans", bans);

        plugin.getCustomFiles().saveBansHistory(gson.fromJson(banHistory, Map.class));
    }

    @Override
    public int getTotalBannedTimesForPlayer(String playerUsername) {
        return loadPlayerBanHistory(playerUsername).size();
    }

    @Override
    public void saveKick(Kick kick) {
        JsonObject kicks = gson.fromJson(gson.toJson(plugin.getCustomFiles().getKicksFile()), JsonObject.class);

        JsonObject id = new JsonObject();

        id.addProperty("kickedByUuid", kick.getKickedByUuid().toString());
        id.addProperty("kickedByUsername", kick.getKickedPlayerUsername());
        id.addProperty("kickedPlayerUsername", kick.getKickedPlayerUsername());
        id.addProperty("kickedPlayerUuid", kick.getKickedByUuid().toString());
        id.addProperty("reason", kick.getReason());
        id.addProperty("date", kick.getDate().toString());

        kicks.add(String.valueOf(kick.getId()), id);

        plugin.getCustomFiles().saveBansHistory(gson.fromJson(kicks, Map.class));

    }

    @Override
    public int getLastKickedId() {
        JsonObject kicks = gson.fromJson(gson.toJson(plugin.getCustomFiles().getKicksFile()), JsonObject.class);

        JsonArray ids = kicks.getAsJsonArray("kicks");

        return ids.size();

    }

    @Override
    public int getKickedTimesForPlayer(String playerName) {
        JsonObject kicks = gson.fromJson(gson.toJson(plugin.getCustomFiles().getKicksFile()), JsonObject.class);

        int count = 0;

        for (Map.Entry<String, JsonElement> elementEntry : kicks.get("kicks").getAsJsonObject().entrySet()) {
            if (kicks.get(elementEntry.getKey()).getAsJsonObject().get("kickedPlayerUsername").getAsString().equals(playerName)) {
                count++;
            }
        }
        return count;
    }

    @Override
    public void saveTextFromChat(SpiedText spiedText) {
        JsonObject spiedTexts = gson.fromJson(gson.toJson(plugin.getCustomFiles().getSpiedText()), JsonObject.class);

        JsonObject uuidSection;
        if (spiedTexts.getAsJsonObject(spiedText.getSenderUuid().toString()) != null) {
            uuidSection = spiedTexts.getAsJsonObject(spiedText.getSenderUuid().toString());
        } else {
            uuidSection = new JsonObject();
            uuidSection.addProperty("spiedPlayerUsername", spiedText.getPlayerUsername());
        }

        JsonObject spiedTextSection;
        if (uuidSection.getAsJsonObject("spiedTexts") != null) {
            spiedTextSection = uuidSection.getAsJsonObject("spiedText");
        } else {
            spiedTextSection = new JsonObject();
        }

        JsonObject dateSection = new JsonObject();
        dateSection.addProperty("text", spiedText.getText());

        spiedTextSection.add(spiedText.getDate().toString(), dateSection);

        uuidSection.add(spiedText.getSenderUuid().toString(), spiedTextSection);

        plugin.getCustomFiles().saveSpiedText(gson.fromJson(uuidSection, Map.class));
    }


    @Override
    public Set<SpiedText> retrieveSpiedText(String playerUsername) {
        JsonObject spiedTexts = gson.fromJson(gson.toJson(plugin.getCustomFiles().getSpiedText()), JsonObject.class);

        Set<SpiedText> spiedTextSet = new HashSet<>();


        for (Map.Entry<String, JsonElement> entry : spiedTexts.entrySet()) {
            if (spiedTexts.getAsJsonObject(entry.getKey()).get("spiedPlayerUsername").getAsString().equalsIgnoreCase(playerUsername)) {
                JsonObject spiedTextsSection = spiedTexts.getAsJsonObject("spiedText");

                for (Map.Entry<String, JsonElement> dates : spiedTexts.entrySet()) {
                    spiedTextSet.add(new SpiedText(UUID.fromString(entry.getKey()), playerUsername, spiedTextsSection.getAsJsonObject(dates.getKey()).get("texts").getAsString(), LocalDateTime.parse(dates.getKey())));
                }
            }
        }

        return spiedTextSet;
    }

    @Override
    public void saveTicket(Ticket ticket) {
        JsonObject tickets = gson.fromJson(gson.toJson(plugin.getCustomFiles().getTicketsFile()), JsonObject.class);

        JsonObject ticketId = new JsonObject();

        ticketId.addProperty("reporterUuid", ticket.getReporterUuid().toString());
        ticketId.addProperty("reporterUsername", ticket.getReporterUsername());
        ticketId.addProperty("smallDescription", ticket.getSmallDescription());
        ticketId.addProperty("description", ticket.getDescription());
        ticketId.addProperty("creationDate", ticket.getCreationDate().toString());

        tickets.add(String.valueOf(ticket.getId()), ticketId);

        plugin.getCustomFiles().saveTicketFiles(gson.fromJson(tickets, Map.class));
    }

    @Override
    public int getAmountOfTickets(String playerUsername) {
        JsonObject tickets = gson.fromJson(gson.toJson(plugin.getCustomFiles().getTicketsFile()), JsonObject.class);

        int count = 0;
        for (Map.Entry<String, JsonElement> element : tickets.entrySet()) {
            if (tickets.get(element.getKey()).getAsJsonObject().get("reporterUsername").getAsString().equals(playerUsername)) {
                count++;
            }
        }

        return count;
    }

    @Override
    public Set<Integer> retrieveTicketsIdForPlayer(String playerUsername) {
        JsonObject tickets = gson.fromJson(gson.toJson(plugin.getCustomFiles().getTicketsFile()), JsonObject.class);

        Set<Integer> ids = new HashSet<>();
        for (Map.Entry<String, JsonElement> element : tickets.entrySet()) {
            if (tickets.get(element.getKey()).getAsJsonObject().get("reporterUsername").getAsString().equals(playerUsername)) {
                ids.add(parseIntegerFromString(element.getKey()));
            }
        }
        return ids;
    }

    @Override
    public Ticket retrieveTicket(int id, String playerUsername) {
        JsonObject tickets = gson.fromJson(gson.toJson(plugin.getCustomFiles().getTicketsFile()), JsonObject.class);

        JsonObject ticket = tickets.get(String.valueOf(id)).getAsJsonObject();

        if (ticket != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            return new Ticket(id, UUID.fromString(ticket.get("reporterUuid").getAsString()), playerUsername, ticket.get("smallDescription").getAsString(), ticket.get("description").getAsString(), LocalDateTime.parse(ticket.get("creationDate").getAsString(), formatter));
        }
        return null;
    }

    @Override
    public boolean verifyTicketId(int id, String playerUsername) {
        JsonObject tickets = gson.fromJson(gson.toJson(plugin.getCustomFiles().getTicketsFile()), JsonObject.class);

        for (Map.Entry<String, JsonElement> element : tickets.entrySet()) {
            if (parseIntegerFromString(element.getKey()) == id && tickets.get(element.getKey()).getAsJsonObject().get("reporterUsername").getAsString().equals(playerUsername)) {
                return true;
            }
        }
        return false;

    }

    @Override
    public int getLastTicketId() {
        JsonObject bans = gson.fromJson(gson.toJson(plugin.getCustomFiles().getTicketsFile()), JsonObject.class);

        JsonArray ids = bans.getAsJsonArray("tickets");

        return ids.size();
    }

    @Override
    public void saveMute(MutedPlayer mutedPlayer) {
        JsonObject mutes = gson.fromJson(gson.toJson(plugin.getCustomFiles().getMutesFile()), JsonObject.class);

        JsonObject muteDetails = new JsonObject();
        muteDetails.addProperty("channelName", mutedPlayer.getChannelName());
        muteDetails.addProperty("mutedByUuid", mutedPlayer.getMutedByUuid().toString());
        muteDetails.addProperty("mutedByUsername", mutedPlayer.getMutedByUsername());
        muteDetails.addProperty("mutedPlayerUuid", mutedPlayer.getMutedPlayerUuid().toString());
        muteDetails.addProperty("mutedPlayerUsername", mutedPlayer.getMutedByUsername());
        muteDetails.addProperty("mutedDate", mutedPlayer.getMutedDate().toString());
        muteDetails.addProperty("releaseDate", mutedPlayer.getReleaseDate().toString());

        mutes.add(String.valueOf(mutedPlayer.getId()), muteDetails);

        plugin.getCustomFiles().saveMutes(gson.fromJson(mutes, Map.class));

        saveMuteToCurrentMutes(mutedPlayer.getId());
    }

    private void saveMuteToCurrentMutes(int muteId) {
        JsonObject currentMutes = gson.fromJson(gson.toJson(plugin.getCustomFiles().getCurrentMutesFile()), JsonObject.class);

        JsonArray mutes;
        if (currentMutes.get("mutes") == null) {
            mutes = new JsonArray();
        } else {
            mutes = currentMutes.get("mutes").getAsJsonArray();
        }

        mutes.add(muteId);

        currentMutes.add("mutes", mutes);
    }

    @Override
    public void removeMute(String playerUsername, String channelName) {
        if (isPlayerMutedByUsername(playerUsername, channelName)) {
            JsonObject mutes = gson.fromJson(gson.toJson(plugin.getCustomFiles().getMutesFile()), JsonObject.class);

            for (Map.Entry<String, JsonElement> elementEntry : mutes.entrySet()) {
                JsonObject muteDetails = mutes.getAsJsonObject(elementEntry.getKey());

                if (muteDetails.get("mutedByUsername").getAsString().equals(playerUsername) && muteDetails.get("channelName").getAsString().equals(channelName)) {
                    removeFromCurrentMutes(parseIntegerFromString(elementEntry.getKey()));
                }
            }
        }
    }

    private void removeFromCurrentMutes(int muteId) {
        JsonObject currentMutes = gson.fromJson(gson.toJson(plugin.getCustomFiles().getCurrentMutesFile()), JsonObject.class);

        JsonArray mutes;
        if (currentMutes.get("mutes") == null) {
            mutes = new JsonArray();
        } else {
            mutes = currentMutes.get("mutes").getAsJsonArray();
        }

        if (mutes.get(muteId) != null) {
            mutes.remove(muteId);
            moveMuteToMutesHistory(muteId);
            plugin.getCustomFiles().saveCurrentMutes(gson.fromJson(currentMutes, Map.class));
        }
    }

    private void moveMuteToMutesHistory(int muteId) {
        JsonObject muteHistory = gson.fromJson(gson.toJson(plugin.getCustomFiles().getMutesHistoryFile()), JsonObject.class);

        JsonArray mutes;
        if (muteHistory.get("mutes") == null) {
            mutes = new JsonArray();
        } else {
            mutes = muteHistory.get("mutes").getAsJsonArray();
        }

        if (mutes.get(muteId) != null) {
            mutes.add(muteId);
            plugin.getCustomFiles().saveHistoryMutes(gson.fromJson(muteHistory, Map.class));

        }
    }

    @Override
    public boolean isPlayerMutedByUuid(UUID playerUuid, String channelName) {
        JsonObject currentMutes = gson.fromJson(gson.toJson(plugin.getCustomFiles().getCurrentMutesFile()), JsonObject.class);

        JsonObject mutes = gson.fromJson(gson.toJson(plugin.getCustomFiles().getMutesFile()), JsonObject.class);

        for (Map.Entry<String, JsonElement> elementEntry : currentMutes.entrySet()) {
            JsonObject muteDetails = mutes.getAsJsonObject(elementEntry.getKey());

            if (muteDetails.get("mutedUuid").getAsString().equals(playerUuid.toString()) && muteDetails.get("channelName").getAsString().equals(channelName)) {
                return true;
            }
        }
        return false;
    }


    @Override
    public boolean isPlayerMutedByUsername(String playerUsername, String channelName) {
        JsonObject currentMutes = gson.fromJson(gson.toJson(plugin.getCustomFiles().getCurrentMutesFile()), JsonObject.class);

        JsonObject mutes = gson.fromJson(gson.toJson(plugin.getCustomFiles().getMutesFile()), JsonObject.class);

        for (Map.Entry<String, JsonElement> elementEntry : currentMutes.entrySet()) {
            JsonObject muteDetails = mutes.getAsJsonObject(elementEntry.getKey());

            if (muteDetails.get("mutedUsername").getAsString().equals(playerUsername) && muteDetails.get("channelName").getAsString().equals(channelName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public MutedPlayer loadMutedPlayer(String playerUsername, String channelName) {
        JsonObject mutes = gson.fromJson(gson.toJson(plugin.getCustomFiles().getMutesFile()), JsonObject.class);

        for (Map.Entry<String, JsonElement> elementEntry : mutes.entrySet()) {
            JsonObject muteDetails = mutes.getAsJsonObject(elementEntry.getKey());

            if (muteDetails.get("mutedByUsername").getAsString().equals(playerUsername) && muteDetails.get("channelName").equals(channelName)) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

                return new MutedPlayer(parseIntegerFromString(elementEntry.getKey()), UUID.fromString(muteDetails.get("mutedByUuid").getAsString()), UUID.fromString(muteDetails.get("mutedPlayerUuid").getAsString()), muteDetails.get("mutedByUsername").getAsString(), muteDetails.get("mutedPlayerUsername").getAsString(), muteDetails.get("channelName").getAsString(), LocalDateTime.parse(muteDetails.get("mutedDate").getAsString(), formatter), LocalDateTime.parse(muteDetails.get("releaseDate").getAsString(), formatter));
            }
        }
        return null;
    }

    @Override
    public int getTotalMutedTimesForPlayer(String playerUsername) {
        JsonObject mutes = gson.fromJson(gson.toJson(plugin.getCustomFiles().getMutesFile()), JsonObject.class);

        int count = 0;

        for (Map.Entry<String, JsonElement> elementEntry : mutes.entrySet()) {
            JsonObject muteDetails = mutes.getAsJsonObject(elementEntry.getKey());

            if (muteDetails.get("mutedByUsername").getAsString().equals(playerUsername)) {
                count++;
            }
        }
        return count;
    }

    @Override
    public int getLastMuteId() {
        JsonObject mutes = gson.fromJson(gson.toJson(plugin.getCustomFiles().getMutesFile()), JsonObject.class);

        JsonArray ids = mutes.getAsJsonArray("mutes");

        return ids.size();
    }
}

package tech.seife.moderation.datamanager.dao;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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
import java.util.logging.Logger;

public class DataManagerFiles implements DataManager {

    private final Gson gson;
    private final Logger logger;
    private final CustomFiles customFiles;

    public DataManagerFiles(CustomFiles customFiles, Logger logger) {
        this.customFiles = customFiles;
        gson = customFiles.getGson();
        this.logger = logger;
    }

    @Override
    public void saveBan(BannedPlayer bannedPlayer) {
        JsonObject allBans = gson.fromJson(gson.toJson(customFiles.getBansFile()), JsonObject.class);

        JsonObject ban = new JsonObject();

        for (Map.Entry<String, JsonElement> entry : allBans.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(bannedPlayer.getBannedUuid().toString())) {
                for (Map.Entry<String, JsonElement> elements : entry.getValue().getAsJsonObject().entrySet()) {
                    ban.add(elements.getKey(), elements.getValue());
                }
                allBans.remove(entry.getKey());
            }
        }


        int banId = 0;

        banId = acquireNewBanId(allBans);

        JsonObject bansDetails = new JsonObject();

        allBans.addProperty("latestId", banId);

        if (!ban.has("bannedPlayerUsername")) {
            ban.addProperty("bannedPlayerUsername", bannedPlayer.getBannedPlayerName());
        }

        addBansDetails(bannedPlayer, banId, bansDetails);

        ban.add(bannedPlayer.getBannedDate().toString(), bansDetails);

        allBans.add(bannedPlayer.getBannedUuid().toString(), ban);

        customFiles.saveBans(gson.fromJson(allBans, Map.class));

        addToCurrentBans(banId);
    }

    private int acquireNewBanId(JsonObject jsonObject) {
        return jsonObject.has("latestId") ? jsonObject.get("latestId").getAsInt() + 1 : 0;
    }

    private void addBansDetails(BannedPlayer bannedPlayer, int banId, JsonObject bansDetails) {
        bansDetails.addProperty("bannedByUuid", bannedPlayer.getBannedUuid().toString());
        bansDetails.addProperty("bannedByName", bannedPlayer.getBannedByName());
        bansDetails.addProperty("reason", bannedPlayer.getReason());
        bansDetails.addProperty("banId", banId);
        bansDetails.addProperty("releaseDate", bannedPlayer.getReleaseDate().toString());
    }

    private void addToCurrentBans(int banId) {
        JsonObject jsonObject = gson.fromJson(gson.toJson(customFiles.getCurrentBansFile()), JsonObject.class);

        JsonArray jsonArray;

        if (!jsonObject.has("banList")) {
            jsonArray = new JsonArray();
        } else {
            jsonArray = jsonObject.getAsJsonArray("banList");
        }

        jsonArray.add(banId);

        jsonObject.add("banList", jsonArray);

        customFiles.saveCurrentBans(gson.fromJson(jsonObject, Map.class));
    }

    @Override
    public void removeBan(BannedPlayer bannedPlayer) {
        if (bannedPlayer != null && isPlayerBannedUuidCheck(bannedPlayer.getBannedByUuid())) {
            JsonObject currentBans = gson.fromJson(gson.toJson(customFiles.getCurrentBansFile()), JsonObject.class);

            JsonArray ids = currentBans.getAsJsonArray("banList");

            for (int i = 0; i < ids.size(); i++) {
                if (ids.get(i).getAsInt() == bannedPlayer.getId()) {
                    ids.remove(i);
                }
            }
            currentBans.remove("banList");

            customFiles.saveCurrentBans(gson.fromJson(currentBans, Map.class));
        }
    }

    @Override
    public boolean isPlayerBannedUuidCheck(UUID playerUuid) {
        JsonObject bans = gson.fromJson(gson.toJson(customFiles.getBansFile()), JsonObject.class);
        JsonObject currentBans = gson.fromJson(gson.toJson(customFiles.getCurrentBansFile()), JsonObject.class);


        if (bans.get(playerUuid.toString()) != null) {
            for (Map.Entry<String, JsonElement> entry : bans.get(playerUuid.toString()).getAsJsonObject().entrySet()) {
                if (entry.getValue().isJsonObject() && entry.getValue().getAsJsonObject().has("banId")) {
                    if (isIdBanned(currentBans, entry)) return true;
                }
            }
        }
        return false;

    }

    @Override
    public boolean isPlayerBannedUsernameCheck(String playerUsername) {
        JsonObject bans = gson.fromJson(gson.toJson(customFiles.getBansFile()), JsonObject.class);
        JsonObject currentBans = gson.fromJson(gson.toJson(customFiles.getCurrentBansFile()), JsonObject.class);


        for (Map.Entry<String, JsonElement> entry : bans.entrySet()) {
            if (entry.getValue().isJsonObject() && entry.getValue().getAsJsonObject().has("bannedPlayerUsername")) {
                if (entry.getValue().getAsJsonObject().get("bannedPlayerUsername").getAsString().equalsIgnoreCase(playerUsername)) {
                    for (Map.Entry<String, JsonElement> details : entry.getValue().getAsJsonObject().entrySet()) {
                        if (details.getValue().isJsonObject() && details.getValue().getAsJsonObject().has("banId")) {
                            if (isIdBanned(currentBans, details)) return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    private boolean isIdBanned(JsonObject currentBans, Map.Entry<String, JsonElement> entry) {
        if (currentBans.getAsJsonArray("banList") == null) return false;

        for (JsonElement element : currentBans.getAsJsonArray("banList")) {
            if (element.getAsInt() == entry.getValue().getAsJsonObject().get("banId").getAsInt()) {
                return true;
            }
        }
        return false;
    }


    @Override
    public Set<BannedPlayer> loadPlayerBanHistory(String playerUsername) {
        JsonObject bansHistory = gson.fromJson(gson.toJson(customFiles.getBansHistoryFile()), JsonObject.class);

        if (bansHistory != null) {
            JsonArray ids = bansHistory.getAsJsonArray("bans");

            JsonObject bans = gson.fromJson(gson.toJson(customFiles.getBansFile()), JsonObject.class);

            Set<BannedPlayer> bannedPlayers = new HashSet<>();

            for (JsonElement element : ids) {
                JsonObject banDetails = bans.getAsJsonObject(element.toString()).getAsJsonObject("bans");
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

                for (Map.Entry<String, JsonElement> detail : banDetails.entrySet()) {
                    if (detail.getKey().equalsIgnoreCase("bannedUsername") && detail.getValue().getAsString().equalsIgnoreCase(playerUsername)) {
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
            JsonObject bans = gson.fromJson(gson.toJson(customFiles.getBansFile()), JsonObject.class);
            JsonObject currentBans = gson.fromJson(gson.toJson(customFiles.getCurrentBansFile()), JsonObject.class);

            for (Map.Entry<String, JsonElement> entry : bans.entrySet()) {
                if (entry.getValue().isJsonObject() && entry.getValue().getAsJsonObject().has("bannedPlayerUsername")) {
                    if (entry.getValue().getAsJsonObject().get("bannedPlayerUsername").getAsString().equalsIgnoreCase(playerUsername)) {
                        for (Map.Entry<String, JsonElement> details : entry.getValue().getAsJsonObject().entrySet()) {
                            if (details.getValue().isJsonObject() && details.getValue().getAsJsonObject().has("banId")) {
                                if (isIdBanned(currentBans, details)) {
                                    JsonObject detailsObj = details.getValue().getAsJsonObject();
                                    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME;

                                    BannedPlayer bannedPlayer = new BannedPlayer(
                                            detailsObj.get("banId").getAsInt(),
                                            UUID.fromString(detailsObj.get("bannedByUuid").getAsString()),
                                            UUID.fromString(entry.getKey()),
                                            entry.getValue().getAsJsonObject().get("bannedPlayerUsername").getAsString(),
                                            detailsObj.get("bannedByName").getAsString(),
                                            detailsObj.get("reason").getAsString(),
                                            LocalDateTime.parse(details.getKey(), dateTimeFormatter),
                                            LocalDateTime.parse(detailsObj.get("releaseDate").getAsString(), dateTimeFormatter));

                                    System.out.println("bannedDate: " + bannedPlayer.getBannedDate());
                                    System.out.println("releaseDate: " + bannedPlayer.getReleaseDate());

                                    return bannedPlayer;
                                }
                            }
                        }
                    }
                }
            }
        }

        return null;
    }

    @Override
    public int getLastBanId() {
        return 0;
    }

    private BannedPlayer getBannedPlayerFromJsonObject(int id, JsonObject banDetails, DateTimeFormatter formatter) {
        return new BannedPlayer(id, UUID.fromString(banDetails.get("bannedUuid").toString()), UUID.fromString(banDetails.get("bannedByUuid").toString()), banDetails.get("bannedUsername").toString(), banDetails.get("bannedByName").toString(), banDetails.get("reason").toString(), LocalDateTime.parse(banDetails.get("bannedDate").toString(), formatter), LocalDateTime.parse(banDetails.get("bannedDate").toString(), formatter));
    }

    private int parseIntegerFromString(String number) {
        try {
            return Integer.parseInt(number);
        } catch (NumberFormatException e) {
            logger.log(Level.WARNING, "Failed to parse integer!\nError message: " + e.getMessage());
            return -1;
        }
    }

    @Override
    public void moveCurrentBanToBanHistory(int banId) {
        JsonObject banHistory = gson.fromJson(gson.toJson(customFiles.getBansHistoryFile()), JsonObject.class);

        JsonArray bans;
        if (banHistory.get("bans") != null) {
            bans = new JsonArray();
        } else {
            bans = banHistory.get("bans").getAsJsonArray();
        }
        bans.add(banId);

        banHistory.add("bans", bans);

        customFiles.saveBansHistory(gson.fromJson(banHistory, Map.class));
    }

    @Override
    public int getTotalBannedTimesForPlayer(String playerUsername) {
        return loadPlayerBanHistory(playerUsername).size();
    }

    @Override
    public void saveKick(Kick kick) {
        JsonObject kicks = gson.fromJson(gson.toJson(customFiles.getKicksFile()), JsonObject.class);

        JsonObject id = new JsonObject();

        id.addProperty("kickedByUuid", kick.getKickedByUuid().toString());
        id.addProperty("kickedByUsername", kick.getKickedPlayerUsername());
        id.addProperty("kickedPlayerUsername", kick.getKickedPlayerUsername());
        id.addProperty("kickedPlayerUuid", kick.getKickedByUuid().toString());
        id.addProperty("reason", kick.getReason());
        id.addProperty("date", kick.getDate().toString());

        kicks.add(String.valueOf(kick.getId()), id);

        customFiles.saveBansHistory(gson.fromJson(kicks, Map.class));

    }

    @Override
    public int getLastKickedId() {
        JsonObject kicks = gson.fromJson(gson.toJson(customFiles.getKicksFile()), JsonObject.class);

        JsonArray ids = kicks.getAsJsonArray("kicks");

        return ids.size();

    }

    @Override
    public int getKickedTimesForPlayer(String playerName) {
        JsonObject kicks = gson.fromJson(gson.toJson(customFiles.getKicksFile()), JsonObject.class);

        int count = 0;

        for (Map.Entry<String, JsonElement> elementEntry : kicks.get("kicks").getAsJsonObject().entrySet()) {
            if (kicks.get(elementEntry.getKey()).getAsJsonObject().get("kickedPlayerUsername").getAsString().equalsIgnoreCase(playerName)) {
                count++;
            }
        }
        return count;
    }

    @Override
    public void saveTextFromChat(SpiedText spiedText) {
        JsonObject spiedTexts = gson.fromJson(gson.toJson(customFiles.getSpiedText()), JsonObject.class);

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

        customFiles.saveSpiedText(gson.fromJson(uuidSection, Map.class));
    }


    @Override
    public Set<SpiedText> retrieveSpiedText(String playerUsername) {
        JsonObject spiedTexts = gson.fromJson(gson.toJson(customFiles.getSpiedText()), JsonObject.class);

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
        JsonObject tickets = gson.fromJson(gson.toJson(customFiles.getTicketsFile()), JsonObject.class);

        JsonObject ticketId = new JsonObject();

        ticketId.addProperty("reporterUuid", ticket.getReporterUuid().toString());
        ticketId.addProperty("reporterUsername", ticket.getReporterUsername());
        ticketId.addProperty("smallDescription", ticket.getSmallDescription());
        ticketId.addProperty("description", ticket.getDescription());
        ticketId.addProperty("creationDate", ticket.getCreationDate().toString());

        tickets.add(String.valueOf(ticket.getId()), ticketId);

        customFiles.saveTicketFiles(gson.fromJson(tickets, Map.class));
    }

    @Override
    public int getAmountOfTickets(String playerUsername) {
        JsonObject tickets = gson.fromJson(gson.toJson(customFiles.getTicketsFile()), JsonObject.class);

        int count = 0;
        for (Map.Entry<String, JsonElement> element : tickets.entrySet()) {
            if (tickets.get(element.getKey()).getAsJsonObject().get("reporterUsername").getAsString().equalsIgnoreCase(playerUsername)) {
                count++;
            }
        }

        return count;
    }

    @Override
    public Set<Integer> retrieveTicketsIdForPlayer(String playerUsername) {
        JsonObject tickets = gson.fromJson(gson.toJson(customFiles.getTicketsFile()), JsonObject.class);

        Set<Integer> ids = new HashSet<>();
        for (Map.Entry<String, JsonElement> element : tickets.entrySet()) {
            if (tickets.get(element.getKey()).getAsJsonObject().get("reporterUsername").getAsString().equalsIgnoreCase(playerUsername)) {
                ids.add(parseIntegerFromString(element.getKey()));
            }
        }
        return ids;
    }

    @Override
    public Ticket retrieveTicket(int id, String playerUsername) {
        JsonObject tickets = gson.fromJson(gson.toJson(customFiles.getTicketsFile()), JsonObject.class);

        JsonObject ticket = tickets.get(String.valueOf(id)).getAsJsonObject();

        if (ticket != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            return new Ticket(id, UUID.fromString(ticket.get("reporterUuid").getAsString()), playerUsername, ticket.get("smallDescription").getAsString(), ticket.get("description").getAsString(), LocalDateTime.parse(ticket.get("creationDate").getAsString(), formatter));
        }
        return null;
    }

    @Override
    public boolean verifyTicketId(int id, String playerUsername) {
        JsonObject tickets = gson.fromJson(gson.toJson(customFiles.getTicketsFile()), JsonObject.class);

        for (Map.Entry<String, JsonElement> element : tickets.entrySet()) {
            if (parseIntegerFromString(element.getKey()) == id && tickets.get(element.getKey()).getAsJsonObject().get("reporterUsername").getAsString().equalsIgnoreCase(playerUsername)) {
                return true;
            }
        }
        return false;

    }

    @Override
    public int getLastTicketId() {
        JsonObject bans = gson.fromJson(gson.toJson(customFiles.getTicketsFile()), JsonObject.class);

        JsonArray ids = bans.getAsJsonArray("tickets");

        return ids.size();
    }

    @Override
    public void saveMute(MutedPlayer mutedPlayer) {
        JsonObject mutes = gson.fromJson(gson.toJson(customFiles.getMutesFile()), JsonObject.class);

        JsonObject muteDetails = new JsonObject();
        muteDetails.addProperty("channelName", mutedPlayer.getChannelName());
        muteDetails.addProperty("mutedByUuid", mutedPlayer.getMutedByUuid().toString());
        muteDetails.addProperty("mutedByUsername", mutedPlayer.getMutedByUsername());
        muteDetails.addProperty("mutedPlayerUuid", mutedPlayer.getMutedPlayerUuid().toString());
        muteDetails.addProperty("mutedPlayerUsername", mutedPlayer.getMutedByUsername());
        muteDetails.addProperty("mutedDate", mutedPlayer.getMutedDate().toString());
        muteDetails.addProperty("releaseDate", mutedPlayer.getReleaseDate().toString());

        mutes.add(String.valueOf(mutedPlayer.getId()), muteDetails);

        customFiles.saveMutes(gson.fromJson(mutes, Map.class));

        saveMuteToCurrentMutes(mutedPlayer.getId());
    }

    private void saveMuteToCurrentMutes(int muteId) {
        JsonObject currentMutes = gson.fromJson(gson.toJson(customFiles.getCurrentMutesFile()), JsonObject.class);

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
            JsonObject mutes = gson.fromJson(gson.toJson(customFiles.getMutesFile()), JsonObject.class);

            for (Map.Entry<String, JsonElement> elementEntry : mutes.entrySet()) {
                JsonObject muteDetails = mutes.getAsJsonObject(elementEntry.getKey());

                if (muteDetails.get("mutedByUsername").getAsString().equalsIgnoreCase(playerUsername) && muteDetails.get("channelName").getAsString().equalsIgnoreCase(channelName)) {
                    removeFromCurrentMutes(parseIntegerFromString(elementEntry.getKey()));
                }
            }
        }
    }

    private void removeFromCurrentMutes(int muteId) {
        JsonObject currentMutes = gson.fromJson(gson.toJson(customFiles.getCurrentMutesFile()), JsonObject.class);

        JsonArray mutes;
        if (currentMutes.get("mutes") == null) {
            mutes = new JsonArray();
        } else {
            mutes = currentMutes.get("mutes").getAsJsonArray();
        }

        if (mutes.get(muteId) != null) {
            mutes.remove(muteId);
            moveMuteToMutesHistory(muteId);
            customFiles.saveCurrentMutes(gson.fromJson(currentMutes, Map.class));
        }
    }

    private void moveMuteToMutesHistory(int muteId) {
        JsonObject muteHistory = gson.fromJson(gson.toJson(customFiles.getMutesHistoryFile()), JsonObject.class);

        JsonArray mutes;
        if (muteHistory.get("mutes") == null) {
            mutes = new JsonArray();
        } else {
            mutes = muteHistory.get("mutes").getAsJsonArray();
        }

        if (mutes.get(muteId) != null) {
            mutes.add(muteId);
            customFiles.saveHistoryMutes(gson.fromJson(muteHistory, Map.class));

        }
    }

    @Override
    public boolean isPlayerMutedByUuid(UUID playerUuid, String channelName) {
        JsonObject currentMutes = gson.fromJson(gson.toJson(customFiles.getCurrentMutesFile()), JsonObject.class);

        JsonObject mutes = gson.fromJson(gson.toJson(customFiles.getMutesFile()), JsonObject.class);

        for (Map.Entry<String, JsonElement> elementEntry : currentMutes.entrySet()) {
            JsonObject muteDetails = mutes.getAsJsonObject(elementEntry.getKey());

            if (muteDetails.get("mutedUuid").getAsString().equalsIgnoreCase(playerUuid.toString()) && muteDetails.get("channelName").getAsString().equalsIgnoreCase(channelName)) {
                return true;
            }
        }
        return false;
    }


    @Override
    public boolean isPlayerMutedByUsername(String playerUsername, String channelName) {
        JsonObject currentMutes = gson.fromJson(gson.toJson(customFiles.getCurrentMutesFile()), JsonObject.class);

        JsonObject mutes = gson.fromJson(gson.toJson(customFiles.getMutesFile()), JsonObject.class);

        for (Map.Entry<String, JsonElement> elementEntry : currentMutes.entrySet()) {
            JsonObject muteDetails = mutes.getAsJsonObject(elementEntry.getKey());

            if (muteDetails.get("mutedUsername").getAsString().equalsIgnoreCase(playerUsername) && muteDetails.get("channelName").getAsString().equalsIgnoreCase(channelName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public MutedPlayer loadMutedPlayer(String playerUsername, String channelName) {
        JsonObject mutes = gson.fromJson(gson.toJson(customFiles.getMutesFile()), JsonObject.class);

        for (Map.Entry<String, JsonElement> elementEntry : mutes.entrySet()) {
            JsonObject muteDetails = mutes.getAsJsonObject(elementEntry.getKey());

            if (muteDetails.get("mutedByUsername").getAsString().equalsIgnoreCase(playerUsername) && muteDetails.get("channelName").equals(channelName)) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

                return new MutedPlayer(parseIntegerFromString(elementEntry.getKey()), UUID.fromString(muteDetails.get("mutedByUuid").getAsString()), UUID.fromString(muteDetails.get("mutedPlayerUuid").getAsString()), muteDetails.get("mutedByUsername").getAsString(), muteDetails.get("mutedPlayerUsername").getAsString(), muteDetails.get("channelName").getAsString(), LocalDateTime.parse(muteDetails.get("mutedDate").getAsString(), formatter), LocalDateTime.parse(muteDetails.get("releaseDate").getAsString(), formatter));
            }
        }
        return null;
    }

    @Override
    public int getTotalMutedTimesForPlayer(String playerUsername) {
        JsonObject mutes = gson.fromJson(gson.toJson(customFiles.getMutesFile()), JsonObject.class);

        int count = 0;

        for (Map.Entry<String, JsonElement> elementEntry : mutes.entrySet()) {
            JsonObject muteDetails = mutes.getAsJsonObject(elementEntry.getKey());

            if (muteDetails.get("mutedByUsername").getAsString().equalsIgnoreCase(playerUsername)) {
                count++;
            }
        }
        return count;
    }

    @Override
    public int getLastMuteId() {
        JsonObject mutes = gson.fromJson(gson.toJson(customFiles.getMutesFile()), JsonObject.class);

        JsonArray ids = mutes.getAsJsonArray("mutes");

        return ids.size();
    }
}

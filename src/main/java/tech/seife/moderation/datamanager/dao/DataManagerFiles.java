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
import java.util.*;
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


        int banId = acquireNewBanId(allBans);

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

        JsonObject uuidSection = new JsonObject();

        if (spiedTexts.has(spiedText.getSenderUuid().toString())) {
            for (Map.Entry<String, JsonElement> entry : spiedTexts.get(spiedText.getSenderUuid().toString()).getAsJsonObject().entrySet()) {
                uuidSection.add(entry.getKey(), entry.getValue());
            }
            spiedTexts.remove(spiedText.getSenderUuid().toString());
            uuidSection.remove("playerUsername");
        }

        uuidSection.addProperty("playerUsername", spiedText.getPlayerUsername());
        uuidSection.addProperty(spiedText.getDate().toString(), spiedText.getText());

        spiedTexts.add(spiedText.getSenderUuid().toString(), uuidSection);

        customFiles.saveSpiedText(gson.fromJson(spiedTexts, Map.class));
    }


    @Override
    public Set<SpiedText> retrieveSpiedText(String playerUsername) {
        JsonObject spiedTexts = gson.fromJson(gson.toJson(customFiles.getSpiedText()), JsonObject.class);

        Set<SpiedText> spiedTextSet = new HashSet<>();

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME;

        for (Map.Entry<String, JsonElement> entry : spiedTexts.entrySet()) {
            if (entry.getValue().getAsJsonObject().has("playerUsername") && entry.getValue().getAsJsonObject().get("playerUsername").getAsString().equalsIgnoreCase(playerUsername)) {
                for (Map.Entry<String, JsonElement> details : entry.getValue().getAsJsonObject().entrySet()) {

                    if (details.getKey() != null && !details.getKey().equalsIgnoreCase("playerUsername")) {
                        spiedTextSet.add(new SpiedText(UUID.fromString(entry.getKey()), playerUsername, details.getValue().getAsString(), LocalDateTime.parse(details.getKey(), dateTimeFormatter)));
                    }
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

        JsonObject uuidSection = new JsonObject();

        int latestId = 0;

        JsonObject channelsMuted = new JsonObject();

        if (mutes.has(mutedPlayer.getMutedPlayerUuid().toString())) {
            for (Map.Entry<String, JsonElement> entry : mutes.get(mutedPlayer.getMutedPlayerUuid().toString()).getAsJsonObject().entrySet()) {
                if (!entry.getKey().equalsIgnoreCase("mutedPlayerName")) {
                    uuidSection.add(entry.getKey(), entry.getValue());
                }
            }

            latestId = mutes.get("latestId").getAsInt();

            uuidSection.remove("latestId");
            uuidSection.remove("mutedPlayerUsername");
        }

        uuidSection.addProperty("mutedPlayerUsername", mutedPlayer.getMutedPlayerUsername());
        mutes.addProperty("latestId", ++latestId);

        if (uuidSection.has(mutedPlayer.getMutedByUuid().toString())) {
            for (Map.Entry<String, JsonElement> entry : uuidSection.getAsJsonObject(mutedPlayer.getMutedByUuid().toString()).getAsJsonObject("channels").entrySet()) {
                channelsMuted.add(entry.getKey(), entry.getValue());
            }
        }

        channelsMuted.addProperty(mutedPlayer.getChannelName(), mutedPlayer.getId());

        JsonObject mutedBy = new JsonObject();

        mutedBy.add("channels", channelsMuted);
        mutedBy.addProperty("mutedByUsername", mutedPlayer.getMutedByUsername());
        mutedBy.addProperty("mutedDate", mutedPlayer.getMutedDate().toString());
        mutedBy.addProperty("releaseDate", mutedPlayer.getReleaseDate().toString());
        mutedBy.addProperty("muteId", mutedPlayer.getId());

        uuidSection.add(mutedPlayer.getMutedByUuid().toString(), mutedBy);


        mutes.add(mutedPlayer.getMutedPlayerUuid().toString(), uuidSection);

        saveMuteToCurrentMutes(mutedPlayer.getId());

        customFiles.saveMutes(gson.fromJson(mutes, Map.class));
    }

    private void saveMuteToCurrentMutes(int muteId) {
        JsonObject currentMutes = gson.fromJson(gson.toJson(customFiles.getCurrentMutesFile()), JsonObject.class);

        JsonArray mutes;
        if (currentMutes.get("mutes") == null) {
            mutes = new JsonArray();
        } else {
            mutes = currentMutes.get("mutes").getAsJsonArray();
            currentMutes.remove("mutes");
        }

        mutes.add(muteId);

        currentMutes.add("mutes", mutes);

        customFiles.saveCurrentMutes(gson.fromJson(currentMutes, Map.class));

    }

    @Override
    public void removeMute(String playerUsername, String channelName) {
        JsonObject currentMutes = gson.fromJson(gson.toJson(customFiles.getCurrentMutesFile()), JsonObject.class);

        if (currentMutes.getAsJsonArray("mutes") == null) return;


        JsonObject mutes = gson.fromJson(gson.toJson(customFiles.getMutesFile()), JsonObject.class);


        Map<String, Integer> map = new HashMap<>();

        for (Map.Entry<String, JsonElement> entry : mutes.entrySet()) {
            map = getListOfMutedChannelsForPlayer(playerUsername, entry);
        }

        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(channelName)) {
                removeFromCurrentMutes(entry.getValue());
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

        for (JsonElement element : mutes) {
            if (element.getAsInt() == muteId) {
                mutes.remove(element);
                moveMuteToMutesHistory(muteId);
                customFiles.saveCurrentMutes(gson.fromJson(currentMutes, Map.class));
                return;
            }
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


        for (JsonElement element : mutes) {
            if (element.getAsInt() == muteId) {
            }
        }
    }

    @Override
    public boolean isPlayerMutedByUuid(UUID playerUuid, String channelName) {
        JsonObject currentMutes = gson.fromJson(gson.toJson(customFiles.getCurrentMutesFile()), JsonObject.class);
        JsonObject mutes = gson.fromJson(gson.toJson(customFiles.getMutesFile()), JsonObject.class);

        if (!mutes.has(playerUuid.toString())) return false;

        JsonObject uuidSection = mutes.getAsJsonObject(playerUuid.toString());


        Set<Integer> idSet = new HashSet<>();

        if (currentMutes.has("mutes")) {
            for (JsonElement element : currentMutes.getAsJsonArray("mutes")) {
                idSet.add(element.getAsInt());
            }
        }

        // TODO: 6/29/2021 Fix this method, not quite good when it comes to performance.
        for (Map.Entry<String, JsonElement> entry : uuidSection.entrySet()) {
            if (entry.getValue().isJsonObject()) {
                for (Map.Entry<String, JsonElement> details : entry.getValue().getAsJsonObject().entrySet()) {
                    if (details.getKey().equalsIgnoreCase("channels")) {
                        for (Map.Entry<String, JsonElement> channel : details.getValue().getAsJsonObject().entrySet()) {
                            if (idSet.contains(channel.getValue().getAsInt())) {
                                return true;
                            }
                        }
                    }
                }
            }
        }


        return false;
    }


    @Override
    public boolean isPlayerMutedByUsername(String playerUsername, String channelName) {
        JsonObject currentMutes = gson.fromJson(gson.toJson(customFiles.getCurrentMutesFile()), JsonObject.class);

        if (currentMutes.getAsJsonArray("mutes") == null) return false;


        JsonObject mutes = gson.fromJson(gson.toJson(customFiles.getMutesFile()), JsonObject.class);


        Map<String, Integer> map = new HashMap<>();

        // TODO: 6/30/2021 It needs optimization.
        for (Map.Entry<String, JsonElement> entry : mutes.entrySet()) {
            map = getListOfMutedChannelsForPlayer(playerUsername, entry);
            for (JsonElement element : currentMutes.getAsJsonArray("mutes")) {
                if (map.containsValue(element.getAsInt())) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public MutedPlayer loadMutedPlayer(String playerUsername, String channelName) {
        JsonObject mutes = gson.fromJson(gson.toJson(customFiles.getMutesFile()), JsonObject.class);

        for (Map.Entry<String, JsonElement> entry : mutes.entrySet()) {
            if (entry.getValue().getAsJsonObject() != null && entry.getValue().getAsJsonObject().has("mutedPlayerUsername")) {
                System.out.println("entry.getKey(): " + entry.getKey());
                System.out.println("entry.getValue  (): " + entry.getValue());
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

        return mutes.has("latestId") ? mutes.get("latestId").getAsInt() : 0;
    }

    private Map<String, Integer> getListOfMutedChannelsForPlayer(String playerUsername, Map.Entry<String, JsonElement> entry) {
        if (entry.getKey().equalsIgnoreCase("latestId")) return null;

        Map<String, Integer> map = new HashMap<>();

        if (entry.getValue().getAsJsonObject().get("mutedPlayerUsername").getAsString().equals(playerUsername)) {
            if (entry.getValue().isJsonObject()) {
                for (Map.Entry<String, JsonElement> details : entry.getValue().getAsJsonObject().entrySet()) {
                    if (details.getValue().isJsonObject() && details.getValue().getAsJsonObject().has("channels")) {
                        for (Map.Entry<String, JsonElement> channelsObject : details.getValue().getAsJsonObject().entrySet()) {
                            if (channelsObject.getKey().equalsIgnoreCase("channels")) {
                                for (Map.Entry<String, JsonElement> channelsDetails : channelsObject.getValue().getAsJsonObject().entrySet()) {
                                    map.put(channelsDetails.getKey(), channelsDetails.getValue().getAsInt());
                                }
                            }
                        }
                    }
                }
            }
        }
        return map;
    }

}

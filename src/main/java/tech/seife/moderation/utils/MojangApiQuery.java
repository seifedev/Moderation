package tech.seife.moderation.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.UUID;

public class MojangApiQuery {

    private static final String NAME_LOCATION = "https://api.mojang.com/users/profiles/minecraft/";
    private static final String UUID_LOCATION = "https://api.mojang.com/user/profiles/<uuid>/names";

    public static String getPlayerNameFromUuid(UUID uuid) {
        String location = UUID_LOCATION;
        location = location.replace("<uuid>", uuid.toString().replaceAll("-", ""));

        StringBuilder json = new StringBuilder();

        try {
            URL url = new URL(location);
            URLConnection connection = url.openConnection();

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                json.append(inputLine);
            }


            Gson gson = new Gson();
            JsonArray jsonObject =  gson.fromJson(json.toString(), JsonArray.class);

            return  jsonObject.get(jsonObject.size() - 1).getAsJsonObject().get("name").getAsString();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;

    }


    public static UUID getPlayersUuidFromName(String name) {
        StringBuilder json = new StringBuilder();

        try {
            URL url = new URL(NAME_LOCATION + name);
            URLConnection connection = url.openConnection();

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                json.append(inputLine);
            }

            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(json.toString(), JsonObject.class);

            return UUID.fromString(jsonObject.get("id").getAsString().replaceFirst("(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5"));

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}

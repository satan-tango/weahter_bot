package com.telegram.bot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.telegram.bot.apiconfig.PositionTrackAPIConfig;
import com.telegram.bot.entity.UserLocation;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class PositionTrackByCoordinateService {

    PositionTrackAPIConfig positionTrackAPIConfig;

    @SneakyThrows
    public UserLocation getLocation(double latitude, double longitude) {
        String response = getDataFromApi(latitude, longitude);
        JSONParser parser = new JSONParser();
        JSONObject rootJsonObject = (JSONObject) parser.parse(response);
        JSONArray userLocationJsonArray = (JSONArray) rootJsonObject.get("data");
        JSONObject userLocationJsonObject = (JSONObject) userLocationJsonArray.get(0);
        String country = (String) userLocationJsonObject.get("country");
        String region = (String) userLocationJsonObject.get("region");
        String locality = (String) userLocationJsonObject.get("locality");
        if (locality == null) {
            locality = (String) userLocationJsonObject.get("county");
        }

        UserLocation userLocation = new UserLocation();
        userLocation.setUserCountry(country);
        userLocation.setUserRegion(region);
        userLocation.setUserLocality(locality);

        return userLocation;
    }

    @SneakyThrows
    private String getDataFromApi(double latitude, double longitude) {
        URL url = new URL("http://api.positionstack.com/v1/reverse" + "?access_key=" +
                positionTrackAPIConfig.getAccessKey() +
                "&query=" + latitude + "," + longitude);

        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return String.valueOf(response);
    }
}

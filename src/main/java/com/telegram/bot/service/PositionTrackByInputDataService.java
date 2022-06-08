package com.telegram.bot.service;

import com.telegram.bot.apiconfig.GeoAPIFyConfig;
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
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class PositionTrackByInputDataService {

    private final GeoAPIFyConfig geoAPIFyConfig;

    @SneakyThrows
    public List<UserLocation> getLocation(String location) {
        String response = getDataFromAPI(location);
        JSONParser parser = new JSONParser();
        JSONObject rootJsonObject = (JSONObject) parser.parse(response);
        JSONArray userLocationJsonArray = (JSONArray) rootJsonObject.get("features");
        List<UserLocation> list = new ArrayList<>();
        for (Object it : userLocationJsonArray) {
            JSONObject userLocationJsonObject = (JSONObject) it;
            userLocationJsonObject = (JSONObject) userLocationJsonObject.get("properties");
            userLocationJsonObject = (JSONObject) userLocationJsonObject.get("address");
            String country = (String) userLocationJsonObject.get("country");
            String region = (String) userLocationJsonObject.get("region");
            if (region == null) {
                region = (String) userLocationJsonObject.get("state");
            }
            if (region != null) {
                region = region.split(" ")[0];
            }
            String locality = (String) userLocationJsonObject.get("city");
            if (locality == null) {
                locality = (String) userLocationJsonObject.get("town");
            }
            if (locality == null) {
                locality = (String) userLocationJsonObject.get("village");
            }
            UserLocation loc = new UserLocation();
            if (country == null || region == null || locality == null) {
                continue;
            }
            loc.setUserCountry(country);
            loc.setUserRegion(region);
            loc.setUserLocality(locality);
            list.add(loc);

        }
        return list;
    }

    @SneakyThrows
    private String getDataFromAPI(String location) {
        final String APIUrl = "https://nominatim.openstreetmap.org/search";
        location = location.replaceAll(" ", "%20");
        URL url = new URL(APIUrl + "?" + "city=" + location +
                "&accept-language=en" +
                "&limit=5" +
                "&format=geojson" +
                "&addressdetails=1");
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

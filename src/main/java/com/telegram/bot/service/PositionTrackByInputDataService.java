package com.telegram.bot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.telegram.bot.entity.UserLocation;
import lombok.SneakyThrows;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

@Service
public class PositionTrackByInputDataService {

    @SneakyThrows
    public List<UserLocation> getLocation(String location) {
        String response = getDataFromAPI(location);
        //   // Map<String, List<Map<String,Maps>>> map = new ObjectMapper().readValue(String.valueOf(response), Map.class);
        return null;
    }

    @SneakyThrows
    private String getDataFromAPI(String location) {
        final String APIUrl = "https://nominatim.openstreetmap.org/search";
        URL url = new URL(APIUrl + "?" + "city=" + location +
                "&format=geojson" +
                "&accept-language=en" +
                "&limit=5" +
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
        JSONObject jsonObject = new JSONObject(response.toString());
        return String.valueOf(response);
    }
}

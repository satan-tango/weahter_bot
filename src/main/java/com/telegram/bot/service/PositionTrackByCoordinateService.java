package com.telegram.bot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.telegram.bot.apiconfig.PositionTrackAPIConfig;
import com.telegram.bot.entity.UserLocation;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
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
        Map<String, List<Map<String, String>>> map = new ObjectMapper().readValue(String.valueOf(response), Map.class);
        List<Map<String, String>> list = (List<Map<String, String>>) map.get("data");
        Map<String, String> data = (Map<String, String>) list.get(0);
        UserLocation userLocation = new UserLocation();
        userLocation.setUserCountry(data.get("country"));
        userLocation.setUserRegion(data.get("region"));
        userLocation.setUserCity(data.get("county"));
        userLocation.setUserLabel(data.get("label"));
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

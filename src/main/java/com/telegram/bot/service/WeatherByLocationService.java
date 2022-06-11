package com.telegram.bot.service;


import com.telegram.bot.apiconfig.WeatherAPIConfig;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.apache.http.client.utils.URIBuilder;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
@AllArgsConstructor
public class WeatherByLocationService {

    private final WeatherAPIConfig weatherAPIConfig;

    @SneakyThrows
    public String getCurrentWeather(String location) {
        String response = getDataFromWeatherApi("current", location);
        JSONParser parser = new JSONParser();
        JSONObject rootJsonObject = (JSONObject) parser.parse(response);

        JSONObject locationUserJsonObject = (JSONObject) rootJsonObject.get("location");
        String localtime = (String) locationUserJsonObject.get("localtime");

        JSONObject currentWeatherJsonObject = (JSONObject) rootJsonObject.get("current");
        JSONObject conditionJsonObject = (JSONObject) currentWeatherJsonObject.get("condition");
        String condition = String.valueOf(conditionJsonObject.get("text"));
        String temperature = String.valueOf(currentWeatherJsonObject.get("temp_c"));
        String feelsLikeTemp = String.valueOf(currentWeatherJsonObject.get("feelslike_c"));
        String windDirection = String.valueOf(currentWeatherJsonObject.get("wind_dir"));
        String windSpeed = String.valueOf(currentWeatherJsonObject.get("wind_kph"));
        String humidity = String.valueOf(currentWeatherJsonObject.get("humidity"));
        String pressure = String.valueOf(currentWeatherJsonObject.get("pressure_mb"));
        String precipitation = String.valueOf(currentWeatherJsonObject.get("precip_mm"));
        String currentWeather = "Weather in " + location + " " + localtime + "\n";
        currentWeather += condition + "\n";
        currentWeather += temperature + "℃, feels like " + feelsLikeTemp + "℃\n";
        currentWeather += "Wind: " + windDirection + " " + windSpeed + " m/h\n";
        currentWeather += "Humidity: " + humidity + " %\n";
        currentWeather += "Pressure: " + pressure + " mm\n";
        currentWeather += "Precipitation: " + precipitation + " mm";
        return currentWeather;
    }

    @SneakyThrows
    private String getDataFromWeatherApi(String type, String location) {
        final String APIUrl = "http://api.weatherapi.com/v1/" + type + ".json";
        URIBuilder uriBuilder = new URIBuilder(APIUrl);
        uriBuilder.addParameter("key", weatherAPIConfig.getAccessKey());
        uriBuilder.addParameter("q", location);
        uriBuilder.addParameter("aqi", "no");

        URL url = uriBuilder.build().toURL();

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

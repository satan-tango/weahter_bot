package com.telegram.bot.service;


import com.telegram.bot.apiconfig.WeatherAPIConfig;
import com.telegram.bot.model.UserCurrentWeather;
import com.telegram.bot.model.UserForecastWeather;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.apache.http.client.utils.URIBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

@Service
@AllArgsConstructor
public class WeatherByLocationService {

    private final WeatherAPIConfig weatherAPIConfig;

    private final Environment environment;

    @SneakyThrows
    public UserCurrentWeather getCurrentWeather(String location) {
        String response = getDataFromWeatherApi("forecast", location, "1");
        JSONParser parser = new JSONParser();
        JSONObject rootJsonObject = (JSONObject) parser.parse(response);

        String briefCurrentWeather = getBriefCurrentWeather(rootJsonObject, location);
        String detailCurrentWeather = getDetailCurrentWeather(rootJsonObject);

        UserCurrentWeather userCurrentWeather = new UserCurrentWeather();
        userCurrentWeather.setBriefCurrentWeather(briefCurrentWeather);
        userCurrentWeather.setDetailCurrentWeather(detailCurrentWeather);
        userCurrentWeather.setHash(userCurrentWeather.hashCode());

        return userCurrentWeather;
    }

    @SneakyThrows
    public UserForecastWeather getForecastWeather(String location) {
        String response = getDataFromWeatherApi("forecast", location, "10");
        JSONParser parser = new JSONParser();
        JSONObject rootJsonObject = (JSONObject) parser.parse(response);
        String briefForecastWeather = getBriefForecastWeather(rootJsonObject, location);
        String detailForecastWeather = getDetailForecastWeather(rootJsonObject, briefForecastWeather);
        UserForecastWeather userForecastWeather = new UserForecastWeather();
        userForecastWeather.setBriefCurrentWeather(briefForecastWeather);
        userForecastWeather.setDetailCurrentWeather(detailForecastWeather);
        userForecastWeather.setHash(userForecastWeather.getHash());
        return userForecastWeather;
    }

    @SneakyThrows
    private String getDetailForecastWeather(JSONObject root, String briefForecastWeather) {
        JSONObject forecastWeatherJsonObject = (JSONObject) root.get("forecast");
        JSONArray forecastDays = (JSONArray) forecastWeatherJsonObject.get("forecastday");
        String detailForecast = "";
        BufferedReader bufferedReader = new BufferedReader(new StringReader(briefForecastWeather));
        String line = "";
        detailForecast += bufferedReader.readLine() + "\n";
        int counter = 0;
        while ((line = bufferedReader.readLine()) != null) {
            detailForecast += line + "\n";
            JSONObject dayObject = (JSONObject) forecastDays.get(counter);
            JSONArray hourJsonArray = (JSONArray) dayObject.get("hour");
            JSONObject hourJsonObject = (JSONObject) hourJsonArray.get(2);
            String nigthTemp = String.valueOf(hourJsonObject.get("temp_c"));
            hourJsonObject = (JSONObject) hourJsonArray.get(8);
            String morningTemp = String.valueOf(hourJsonObject.get("temp_c"));
            hourJsonObject = (JSONObject) hourJsonArray.get(14);
            String dayTemp = String.valueOf(hourJsonObject.get("temp_c"));
            hourJsonObject = (JSONObject) hourJsonArray.get(20);
            String eveningTemp = String.valueOf(hourJsonObject.get("temp_c"));
            detailForecast += "\uD83D\uDD39 night: " + nigthTemp + "℃\n";
            detailForecast += "\uD83D\uDD39 morning: " + morningTemp + "℃\n";
            detailForecast += "\uD83D\uDD39 day: " + dayTemp + "℃\n";
            detailForecast += "\uD83D\uDD39 evening: " + eveningTemp + "℃\n";
            counter++;
        }
        return detailForecast;
    }

    private String getBriefForecastWeather(JSONObject root, String location) {
        String briefForecast = "";
        JSONObject forecastWeatherJsonObject = (JSONObject) root.get("forecast");
        JSONArray forecastDays = (JSONArray) forecastWeatherJsonObject.get("forecastday");
        briefForecast += "Weather forecast in *" + location + "*\n";
        for (int i = 0; i < forecastDays.size(); i++) {
            JSONObject dayObject = (JSONObject) forecastDays.get(i);
            String date = String.valueOf(dayObject.get("date"));
            JSONObject day = (JSONObject) dayObject.get("day");
            String maxTemp = String.valueOf(day.get("maxtemp_c"));
            String minTemp = String.valueOf(day.get("mintemp_c"));
            JSONObject conditionObject = (JSONObject) day.get("condition");
            String condition = String.valueOf(conditionObject.get("text"));
            briefForecast +="\uD83D\uDCC3"+ date + ": from " + minTemp + "℃ to " + maxTemp + "℃\n";
        }
        return briefForecast;
    }

    @SneakyThrows
    private String getDataFromWeatherApi(String type, String location, String days) {
        final String APIUrl = "http://api.weatherapi.com/v1/" + type + ".json";
        URIBuilder uriBuilder = new URIBuilder(APIUrl);
        uriBuilder.addParameter("key", weatherAPIConfig.getAccessKey());
        uriBuilder.addParameter("q", location);
        uriBuilder.addParameter("days", days);
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


    private String getBriefCurrentWeather(JSONObject root, String location) {
        JSONObject currentWeatherJsonObject = (JSONObject) root.get("current");
        JSONObject conditionJsonObject = (JSONObject) currentWeatherJsonObject.get("condition");
        String condition = String.valueOf(conditionJsonObject.get("text"));
        String conditionCode = String.valueOf(conditionJsonObject.get("code"));
        String temperature = String.valueOf(currentWeatherJsonObject.get("temp_c"));
        String feelsLikeTemp = String.valueOf(currentWeatherJsonObject.get("feelslike_c"));
        String windDirection = String.valueOf(currentWeatherJsonObject.get("wind_dir"));
        String windSpeed = String.valueOf(currentWeatherJsonObject.get("wind_kph"));
        String humidity = String.valueOf(currentWeatherJsonObject.get("humidity"));
        String pressure = String.valueOf(currentWeatherJsonObject.get("pressure_mb"));
        String chanceOfRain = getChanceOfRain(root);

        String dataAndTime = getDataAndTime(root);
        String briefCurrentWeather = "Weather in *" + location + "*\n " + dataAndTime + "\n\n";
        briefCurrentWeather += condition + "\n";
        briefCurrentWeather += temperature + "℃, feels like " + feelsLikeTemp + "℃\n";
        briefCurrentWeather += "\uD83D\uDCA8Wind: " + windDirection + " " + windSpeed + " m/h\n";
        briefCurrentWeather += "\uD83D\uDCA6Humidity: " + humidity + "%\n";
        briefCurrentWeather += "\uD83C\uDF21Pressure: " + pressure + "mm\n";
        briefCurrentWeather += "☔Precipitation: " + chanceOfRain + "%\n\n";
        return briefCurrentWeather;
    }

    private String getDataAndTime(JSONObject root) {
        JSONObject locationUserJsonObject = (JSONObject) root.get("location");
        String dataAndTime = (String) locationUserJsonObject.get("localtime");
        return dataAndTime;
    }

    private String getDetailCurrentWeather(JSONObject root) {
        JSONObject forecastWeatherJsonObject = (JSONObject) root.get("forecast");
        JSONArray forecastDayWeatherJsonObject = (JSONArray) forecastWeatherJsonObject.get("forecastday");
        JSONObject forecastDataJsonObject = (JSONObject) forecastDayWeatherJsonObject.get(0);
        String detailCurrentWeather = "";

        JSONObject astro = (JSONObject) forecastDataJsonObject.get("astro");
        String sunrise = String.valueOf(astro.get("sunrise"));
        String sunset = String.valueOf(astro.get("sunset"));

        JSONObject day = (JSONObject) forecastDataJsonObject.get("day");
        String maxTemperature = String.valueOf(day.get("maxtemp_c"));
        String minTemperature = String.valueOf(day.get("mintemp_c"));
        String dailyChanceOfRain = String.valueOf(day.get("daily_chance_of_rain"));

        detailCurrentWeather = "Predicted from " + minTemperature + "℃ to " + maxTemperature + "℃\n";
        detailCurrentWeather += "Daily chane of rain: " + dailyChanceOfRain + "%\n";

        JSONArray hours = (JSONArray) forecastDataJsonObject.get("hour");
        for (int i = 0; i < 24; i++) {
            if (i % 2 == 0) {
                JSONObject hour = (JSONObject) hours.get(i);
                String time = String.valueOf(hour.get("time")).split(" ")[1];
                String temp = String.valueOf(hour.get("temp_c"));
                String windSpeed = String.valueOf(hour.get("wind_kph"));
                String chanceOfRain = String.valueOf((hour.get("chance_of_rain")));
                JSONObject conditionJsonObject = (JSONObject) hour.get("condition");
                String condition = String.valueOf(conditionJsonObject.get("text"));
                if (Integer.parseInt(chanceOfRain) > 0) {
                    detailCurrentWeather += "•" + time + ": " + temp + "℃  \uD83D\uDCA8" +
                            windSpeed + "  ☂" + chanceOfRain + "% " + condition + "\n";
                } else {
                    detailCurrentWeather += "•" + time + ": " + temp + "℃  \uD83D\uDCA8" +
                            windSpeed + "  " + condition + "\n";
                }
            }
        }
        detailCurrentWeather += "\n\n•\uD83C\uDF18: " + sunrise + "\n";
        detailCurrentWeather += "•\uD83C\uDF11: " + sunset;
        return detailCurrentWeather;
    }

    private String getChanceOfRain(JSONObject root) {
        String time = getDataAndTime(root).split(" ")[1];
        String inaccurateTime = "";
        String chanceOfRain = "";
        if (Integer.parseInt(time.split(":")[1]) >= 30) {
            if (Integer.parseInt(time.split(":")[0]) + 1 == 24) {
                inaccurateTime = "00:00";
            } else {
                inaccurateTime = String.valueOf(Integer.parseInt(time.split(":")[0]) + 1) + ":00";
            }
        } else {
            inaccurateTime = time.split(":")[0] + ":00";
        }
        if (inaccurateTime.length() != 5) {
            inaccurateTime = "0" + inaccurateTime;
        }

        JSONObject forecastWeatherJsonObject = (JSONObject) root.get("forecast");
        JSONArray forecastDayWeatherJsonObject = (JSONArray) forecastWeatherJsonObject.get("forecastday");
        JSONObject forecastDataJsonObject = (JSONObject) forecastDayWeatherJsonObject.get(0);
        JSONArray hours = (JSONArray) forecastDataJsonObject.get("hour");
        for (int i = 0; i < 24; i++) {
            JSONObject hour = (JSONObject) hours.get(i);
            String dayTime = String.valueOf(hour.get("time")).split(" ")[1];
            if (dayTime.equals(inaccurateTime)) {
                return chanceOfRain = String.valueOf((hour.get("chance_of_rain")));
            }

        }
        return chanceOfRain;
    }

//    private String getEmojiByConditionCode(String code) {
//        return String.valueOf(environment.getProperty("condition.code." + code));
//    }


}

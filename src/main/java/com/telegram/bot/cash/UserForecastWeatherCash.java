package com.telegram.bot.cash;

import com.telegram.bot.model.UserCurrentWeather;
import com.telegram.bot.model.UserForecastWeather;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Getter
public class UserForecastWeatherCash {
    private final Map<Long, List<UserForecastWeather>> userForecastWeatherMap = new HashMap<>();

    public void saveForecastWeather(long userId, UserForecastWeather userForecastWeather) {
        if (userForecastWeatherMap.get(userId) == null) {
            userForecastWeatherMap.put(userId, new LinkedList<>(Arrays.asList(userForecastWeather)));
        } else {
            if (userForecastWeatherMap.get(userId).size() >= 20) {
                userForecastWeatherMap.get(userId).remove(0);
            }
            List<UserForecastWeather> list = userForecastWeatherMap.get(userId);
            if (list.stream().anyMatch(forecastWeatherList -> forecastWeatherList.getHash() == userForecastWeather.getHash())) {
                return;
            }
            list.add(userForecastWeather);
            userForecastWeatherMap.put(userId, list);
        }
    }

    public UserForecastWeather getUserForecastWeatherByHash(long userId, long hashCode) {
        if (userForecastWeatherMap.get(userId) == null) {
            return null;
        }
        List<UserForecastWeather> list = userForecastWeatherMap.get(userId);
        if (list.isEmpty()) {
            return null;
        }
        if (list.stream().anyMatch(forecastWeather -> forecastWeather.getHash() == hashCode)) {
            return list.stream().filter(currentWeather -> currentWeather.getHash() == hashCode)
                    .findFirst().get();
        }
        return null;
    }
}

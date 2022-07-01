package com.telegram.bot.cash;

import com.telegram.bot.model.UserCurrentWeather;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Getter
public class UserCurrentWeatherCash {

    private final Map<Long, List<UserCurrentWeather>> userCurrentWeatherMap = new HashMap<>();

    public void saveCurrentWeather(long userId, UserCurrentWeather userCurrentWeather) {
        if (userCurrentWeatherMap.get(userId) == null) {
            userCurrentWeatherMap.put(userId, new LinkedList<>(Arrays.asList(userCurrentWeather)));
        } else {
            if (userCurrentWeatherMap.get(userId).size() >= 20) {
                userCurrentWeatherMap.get(userId).remove(0);
            }
            List<UserCurrentWeather> list = userCurrentWeatherMap.get(userId);
            if (list.stream().anyMatch(currentWeatherList -> currentWeatherList.getHash() == userCurrentWeather.getHash())) {
                return;
            }
            list.add(userCurrentWeather);
            userCurrentWeatherMap.put(userId, list);
        }
    }

    public UserCurrentWeather getUserCurrentWeatherByHash(long userId, long hashCode) {
        if (userCurrentWeatherMap.get(userId) == null) {
            return null;
        }
        List<UserCurrentWeather> list = userCurrentWeatherMap.get(userId);
        if (list.isEmpty()) {
            return null;
        }
        if (list.stream().anyMatch(currentWeather -> currentWeather.getHash() == hashCode)) {
            return list.stream().filter(currentWeather -> currentWeather.getHash() == hashCode)
                    .findFirst().get();
        }
        return null;
    }

}

package com.telegram.bot.cash;


import com.telegram.bot.entity.UserLocation;
import com.telegram.bot.model.BotState;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Getter
@Setter
public class UserLocationCash {

    private final Map<Long, List<UserLocation>> userLocationMap = new HashMap<>();

    public void saveUserLocation(long userId, List<UserLocation> list) {
        userLocationMap.put(userId, list);
    }

    public void deleteUserLocation(long userId) {
        userLocationMap.remove(userId);
    }
}

package com.telegram.bot.cash;


import com.telegram.bot.entity.UserLocation;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Getter
@Setter
public class UserLocationCash {

    private final Map<Long, Map<Integer, List<UserLocation>>> userLocationMap = new HashMap<>();

    public int saveUserLocation(long userId, List<UserLocation> list) {
        if (userLocationMap.get(userId) == null ||
                userLocationMap.get(userId).size() == 0) {
            Map<Integer, List<UserLocation>> map = new LinkedHashMap<>();
            map.put(0, list);
            userLocationMap.put(userId, map);
            return 0;
        } else {
            Map<Integer, List<UserLocation>> map = userLocationMap.get(userId);
            for (Map.Entry<Integer, List<UserLocation>> entry : map.entrySet()) {
                if (list.equals(entry.getValue())) {
                    return entry.getKey();
                }
            }
            if (map.size() >= 15) {
                map.remove((int) map.keySet().toArray()[0]);
            }
            int lastIndex = (int) map.keySet().toArray()[map.keySet().toArray().length - 1];
            map.put(lastIndex + 1, list);
            userLocationMap.put(userId, map);
            return lastIndex + 1;
        }
    }

    public UserLocation getUserLocationListByKey(long userId, int mapKey, int index) {
        if (userLocationMap.get(userId) == null) {
            return null;
        }
        Map<Integer, List<UserLocation>> map = userLocationMap.get(userId);
        List<UserLocation> list = map.get(mapKey);
        if (list == null) {
            return null;
        }
        return list.get(index);
    }


    public void deleteUserLocation(long userId, int mapKey) {
        Map<Integer, List<UserLocation>> map = userLocationMap.get(userId);
        if (map.get(mapKey) == null) {
            return;
        }
        map.remove(mapKey);
        userLocationMap.put(userId, map);
    }
}

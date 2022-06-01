package com.telegram.bot.DAO;

import com.telegram.bot.entity.User;
import com.telegram.bot.entity.UserLocation;
import com.telegram.bot.repository.UserLocationRepository;
import com.telegram.bot.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class UserLocationDAO {

    private final UserRepository userRepository;

    private final UserLocationRepository userLocationRepository;

    public List<UserLocation> findByUserID(long userId) {
        User user = userRepository.findByUserId(userId);
        return user.getLocations();
    }

    public List<UserLocation> findAllLocation() {
        return userLocationRepository.findAll();
    }

    public UserLocation findByLocationId(long locationId) {
        return userLocationRepository.findByLocationId(locationId);
    }

    public void removeLocation(UserLocation userLocation) {
        userLocationRepository.delete(userLocation);
    }

    public void saveLocation(UserLocation userLocation) {
        userLocationRepository.save(userLocation);
    }

    public void deleteLocation(long locationId) {
        userLocationRepository.deleteById(locationId);
    }
}

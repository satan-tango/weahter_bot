package com.telegram.bot.DAO;

import com.telegram.bot.entity.User;
import com.telegram.bot.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class UserDAO {

    private final UserRepository userRepository;

    public User findByUserId(long userId) {
        return userRepository.findByUserId(userId);
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public void removeUser(long userID) {
        userRepository.delete(findByUserId(userID));
    }

    public void saveUser(User user) {
        userRepository.save(user);
    }

    public boolean isExist(long userId) {
        User user = findByUserId(userId);
        return user != null;
    }
}

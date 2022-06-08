package com.telegram.bot.model.handler;

import com.telegram.bot.DAO.UserDAO;
import com.telegram.bot.DAO.UserLocationDAO;
import com.telegram.bot.cash.UserLocationCash;
import com.telegram.bot.entity.User;
import com.telegram.bot.entity.UserLocation;
import com.telegram.bot.service.MenuService;
import com.telegram.bot.service.PositionTrackByCoordinateService;
import com.telegram.bot.service.PositionTrackByInputDataService;
import com.telegram.bot.service.ValidateInputDataService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.ArrayList;
import java.util.List;

@Component
@AllArgsConstructor
public class EventHandler {

    private final UserDAO userDAO;

    private final UserLocationDAO userLocationDAO;

    private final PositionTrackByCoordinateService positionTrackByCoordinateService;

    private final PositionTrackByInputDataService positionTrackByInputDataService;

    private final ValidateInputDataService validateInputDataService;

    private final MenuService menuService;

    private final UserLocationCash userLocationCash;


    public void saveNewUser(Message message, long userId) {
        String name = message.getFrom().getFirstName();
        String userName = message.getFrom().getUserName();
        User user = new User();
        user.setUserId(userId);
        user.setName(name);
        user.setUserName(userName);
        userDAO.saveUser(user);
    }

    public BotApiMethod<?> saveLocationByGPS(long chatId, long userId, Message message) {
        Double latitude = message.getLocation().getLatitude();
        Double longitude = message.getLocation().getLongitude();
        UserLocation userLocation = positionTrackByCoordinateService.getLocation(latitude, longitude);
        User user = userDAO.findByUserId(userId);
        userLocation.setUser(user);
        if (userLocationDAO.findByUserID(userId)
                .stream().
                filter(loc -> loc.getUserCountry().equals(userLocation.getUserCountry()) &&
                        loc.getUserRegion().equals(userLocation.getUserRegion()) &&
                        loc.getUserLocality().equals(userLocation.getUserLocality()))
                .findAny()
                .isEmpty()) {
            userLocationDAO.saveLocation(userLocation);
            return menuService.getMainMenuMessage(chatId, "Location successfully saved", userId);
        }
        return menuService.getMainMenuMessage(chatId, "This location is already exist", userId);
    }

    public BotApiMethod<?> saveLocationByChat(long chatId, long userID, Message message) {
        String location = message.getText();
        if (!validateInputDataService.validateInputLocation(location)) {
            return menuService.getPositionMenuMessage(chatId, "Invalid location", userID);
        }

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        List<UserLocation> list = positionTrackByInputDataService.getLocation(location);

        if (list.isEmpty()) {
            sendMessage.setText("Location not found");
            return sendMessage;
        }

        userLocationCash.saveUserLocation(userID, list);
        sendMessage.setText("Pick your location");
        sendMessage.setReplyMarkup(menuService.getInlineKeyboardForAddLocation(list));
        return sendMessage;
    }

    public BotApiMethod<?> saveLocation(int index, CallbackQuery callbackQuery) {
        long chatId = callbackQuery.getMessage().getChatId();
        long userId = callbackQuery.getFrom().getId();
        EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup();
        editMessageReplyMarkup.setChatId(String.valueOf(chatId));
        editMessageReplyMarkup.setMessageId(callbackQuery.getMessage().getMessageId());
        editMessageReplyMarkup.setReplyMarkup(null);
        if (userLocationCash.getUserLocationMap().get(userId).isEmpty()) {
            return editMessageReplyMarkup;
        }
        UserLocation userLocation = userLocationCash.getUserLocationMap().get(userId).get(index);
        if (userLocationDAO.findByUserID(userId)
                .stream().
                filter(loc -> loc.getUserCountry().equals(userLocation.getUserCountry()) &&
                        loc.getUserRegion().equals(userLocation.getUserRegion()) &&
                        loc.getUserLocality().equals(userLocation.getUserLocality()))
                .findAny()
                .isEmpty()) {
            userLocation.setUser(userDAO.findByUserId(userId));
            userLocationDAO.saveLocation(userLocation);
            userLocationCash.deleteUserLocation(userId);
            return editMessageReplyMarkup;
        }
        userLocationCash.deleteUserLocation(userId);
        return editMessageReplyMarkup;
    }

    public BotApiMethod<?> deleteLocation(long locationId, CallbackQuery buttonQuery) {
        long userId = buttonQuery.getFrom().getId();
        long chatId = buttonQuery.getMessage().getChatId();
        userLocationDAO.deleteLocation(locationId);
        List<UserLocation> userLocation = userLocationDAO.findByUserID(userId);
        EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup();
        editMessageReplyMarkup.setChatId(String.valueOf(chatId));
        editMessageReplyMarkup.setMessageId(buttonQuery.getMessage().getMessageId());
        if (userLocation.isEmpty()) {
            editMessageReplyMarkup.setReplyMarkup(null);
            return editMessageReplyMarkup;
        }
        editMessageReplyMarkup.setReplyMarkup(menuService.getInlineKeyboardForDeleteLocation(userLocation));

        return editMessageReplyMarkup;
    }

    public BotApiMethod<?> queryLocationButtonToDelete(long chatId, long userId) {
        List<UserLocation> userLocation = userLocationDAO.findByUserID(userId);
        SendMessage sendMessage;
        if (userLocation.isEmpty()) {
            return menuService.getLocationMenuMessage(chatId, "There's no location to delete", userId);
        }
        sendMessage = menuService.getInlineMessageForDeleteLocation(chatId, userLocation);

        return sendMessage;

    }
}

package com.telegram.bot.model.handler;

import com.telegram.bot.DAO.UserDAO;
import com.telegram.bot.DAO.UserLocationDAO;
import com.telegram.bot.cash.BotStateCash;
import com.telegram.bot.cash.UserLocationCash;
import com.telegram.bot.config.ApplicationContextProvider;
import com.telegram.bot.entity.User;
import com.telegram.bot.entity.UserLocation;
import com.telegram.bot.model.TelegramBot;
import com.telegram.bot.service.*;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;


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

    private final BotStateCash botStateCash;

    private final WeatherByLocationService weatherByLocationService;


    public void saveNewUser(Message message, long userId) {
        String name = message.getFrom().getFirstName();
        String userName = message.getFrom().getUserName();
        User user = new User();
        user.setUserId(userId);
        user.setName(name);
        user.setUserName(userName);
        userDAO.saveUser(user);
    }

    public BotApiMethod<?> saveLocationByCoordinate(long chatId, long userId, Message message) {
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
            botStateCash.cleaningBotSateCash(chatId);
            return menuService.getMainMenuMessage(chatId, "Location successfully saved", userId);
        }
        botStateCash.cleaningBotSateCash(chatId);
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
        sendMessage.setText("Select the desired location");
        sendMessage.setReplyMarkup(menuService.getInlineKeyboardForAddLocation(list));
        return sendMessage;
    }


    @SneakyThrows
    public BotApiMethod<?> saveLocation(int index, CallbackQuery callbackQuery) {
        long chatId = callbackQuery.getMessage().getChatId();
        long userId = callbackQuery.getFrom().getId();
        EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup();
        editMessageReplyMarkup.setChatId(String.valueOf(chatId));
        editMessageReplyMarkup.setMessageId(callbackQuery.getMessage().getMessageId());
        editMessageReplyMarkup.setReplyMarkup(null);

        SendMessage sendMessage = new SendMessage();
        TelegramBot telegramBot = ApplicationContextProvider.getApplicationContext().getBean(TelegramBot.class);
        sendMessage.setChatId(String.valueOf(chatId));
        if (userLocationCash.getUserLocationMap().get(userId) == null) {
            sendMessage.setText("Invalid data, do operation from the begging");
            telegramBot.execute(sendMessage);
            telegramBot.execute(editMessageReplyMarkup);

            return menuService.getMainMenuMessage(chatId, "Main menu", userId);
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


            sendMessage.setText("Location successfully saved");
            botStateCash.cleaningBotSateCash(chatId);
            telegramBot.execute(sendMessage);
            telegramBot.execute(editMessageReplyMarkup);

            return menuService.getMainMenuMessage(chatId, "Main menu", userId);
        }
        userLocationCash.deleteUserLocation(userId);

        sendMessage.setText("Location is already exist");
        telegramBot.execute(sendMessage);

        return editMessageReplyMarkup;
    }

    public BotApiMethod<?> deleteLocation(long locationId, CallbackQuery buttonQuery) {
        long userId = buttonQuery.getFrom().getId();
        long chatId = buttonQuery.getMessage().getChatId();
        EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup();
        editMessageReplyMarkup.setChatId(String.valueOf(chatId));
        editMessageReplyMarkup.setMessageId(buttonQuery.getMessage().getMessageId());
        if (userLocationDAO.findByLocationId(locationId) == null) {
            editMessageReplyMarkup.setReplyMarkup(null);
            return editMessageReplyMarkup;
        }
        userLocationDAO.deleteLocation(locationId);

        List<UserLocation> userLocations = userLocationDAO.findByUserID(userId);
        if (userLocations.isEmpty()) {
            editMessageReplyMarkup.setReplyMarkup(null);
            return editMessageReplyMarkup;
        }
        editMessageReplyMarkup.setReplyMarkup(menuService.getInlineKeyboardForDeleteLocation(userLocations));

        return editMessageReplyMarkup;
    }

    @SneakyThrows
    public BotApiMethod<?> weatherStartPage(long chatId, long userId) {
        TelegramBot telegramBot = ApplicationContextProvider.getApplicationContext().getBean(TelegramBot.class);
        List<UserLocation> userLocations = userLocationDAO.findByUserID(userId);
        if (userLocations.isEmpty()) {
            return menuService.getPositionMenuMessage(chatId, "Yoh have not added any location", userId);
        }
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText("Choose a location\uD83C\uDF1A");
        sendMessage.setReplyMarkup(menuService.getInlineKeyboardForWeather(userLocations));
        telegramBot.execute(sendMessage);

        SendMessage sendMessage1 = new SendMessage();
        sendMessage1.setChatId(String.valueOf(chatId));
        sendMessage1.setText("Location menu\uD83E\uDDED");
        sendMessage1.setReplyMarkup(menuService.getWeatherMenuKeyboard());
        return sendMessage1;
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

    public BotApiMethod<?> showWeather(long locationId, CallbackQuery buttonQuery) {
        long userId = buttonQuery.getFrom().getId();
        long chatId = buttonQuery.getMessage().getChatId();
        EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup();
        editMessageReplyMarkup.setChatId(String.valueOf(chatId));
        editMessageReplyMarkup.setMessageId(buttonQuery.getMessage().getMessageId());
        if (userLocationDAO.findByLocationId(locationId) == null) {
            editMessageReplyMarkup.setReplyMarkup(null);
            return editMessageReplyMarkup;
        }
        String location = userLocationDAO.findByLocationId(locationId).getUserLocality();
        String currentWeather = weatherByLocationService.getCurrentWeather(location);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(currentWeather);
        return sendMessage;
    }
}

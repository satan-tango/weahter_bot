package com.telegram.bot.model.handler;

import com.telegram.bot.DAO.UserDAO;
import com.telegram.bot.DAO.UserLocationDAO;
import com.telegram.bot.cash.BotStateCash;
import com.telegram.bot.cash.UserCurrentWeatherCash;
import com.telegram.bot.cash.UserForecastWeatherCash;
import com.telegram.bot.cash.UserLocationCash;
import com.telegram.bot.config.ApplicationContextProvider;
import com.telegram.bot.entity.User;
import com.telegram.bot.entity.UserLocation;
import com.telegram.bot.model.BotState;
import com.telegram.bot.model.TelegramBot;
import com.telegram.bot.model.UserCurrentWeather;
import com.telegram.bot.model.UserForecastWeather;
import com.telegram.bot.service.*;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
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

    private final UserCurrentWeatherCash userCurrentWeatherCash;

    private final UserForecastWeatherCash userForecastWeatherCash;


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
            if (userLocationDAO.findByUserID(userId).isEmpty()) {
                userLocation.setLocationForWeatherByDefault(true);
            } else {
                userLocation.setLocationForWeatherByDefault(false);
            }
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
            return menuService.getPositionMenuMessage(chatId, "Invalid location", userID, false);
        }

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        List<UserLocation> list = positionTrackByInputDataService.getLocation(location);

        if (list.isEmpty()) {
            sendMessage.setText("Location not found");
            return sendMessage;
        }

        int mapKey = userLocationCash.saveUserLocation(userID, list);
        sendMessage.setText("Select the desired location");
        sendMessage.setReplyMarkup(menuService.getInlineKeyboardForAddLocation(list, mapKey));
        return sendMessage;
    }


    @SneakyThrows
    public BotApiMethod<?> saveLocation(int mapKey, int index, CallbackQuery callbackQuery) {
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
        UserLocation userLocation = userLocationCash.getUserLocationListByKey(userId, mapKey, index);
        if (userLocation == null) {
            sendMessage.setText("Invalid data, do operation from the begging");
            telegramBot.execute(sendMessage);
            telegramBot.execute(editMessageReplyMarkup);

            return menuService.getMainMenuMessage(chatId, "Main menu", userId);
        }
        if (userLocationDAO.findByUserID(userId)
                .stream().
                filter(loc -> loc.getUserCountry().equals(userLocation.getUserCountry()) &&
                        loc.getUserRegion().equals(userLocation.getUserRegion()) &&
                        loc.getUserLocality().equals(userLocation.getUserLocality()))
                .findAny()
                .isEmpty()) {
            userLocation.setUser(userDAO.findByUserId(userId));
            if (userLocationDAO.findByUserID(userId).isEmpty()) {
                userLocation.setLocationForWeatherByDefault(true);
            } else {
                userLocation.setLocationForWeatherByDefault(false);
            }
            userLocationDAO.saveLocation(userLocation);
            userLocationCash.deleteUserLocation(userId, mapKey);


            sendMessage.setText("Location successfully saved");
            botStateCash.cleaningBotSateCash(chatId);
            telegramBot.execute(sendMessage);
            telegramBot.execute(editMessageReplyMarkup);

            return menuService.getMainMenuMessage(chatId, "Main menu", userId);
        }
        userLocationCash.deleteUserLocation(userId, mapKey);

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
        if (userLocationDAO.findByLocationId(locationId).getLocationForWeatherByDefault()) {
            userLocationDAO.deleteLocationByID(locationId);
            List<UserLocation> list = userLocationDAO.findByUserID(userId);
            if (!list.isEmpty()) {
                UserLocation userLocation = list.get(0);
                userLocation.setLocationForWeatherByDefault(true);
                userLocationDAO.saveLocation(userLocation);
            }
        } else {
            userLocationDAO.deleteLocationByID(locationId);
        }


        List<UserLocation> userLocations = userLocationDAO.findByUserID(userId);
        if (userLocations.isEmpty()) {
            editMessageReplyMarkup.setReplyMarkup(null);
            return editMessageReplyMarkup;
        }
        editMessageReplyMarkup.setReplyMarkup(menuService.getInlineKeyboardForDeleteLocation(userLocations));

        return editMessageReplyMarkup;
    }

    @SneakyThrows
    public BotApiMethod<?> currentWeatherStartPage(long chatId, long userId) {
        TelegramBot telegramBot = ApplicationContextProvider.getApplicationContext().getBean(TelegramBot.class);
        if (userLocationDAO.findByUserID(userId).isEmpty()) {
            botStateCash.saveBotState(chatId, BotState.ADD_LOCATION);
            return menuService.getPositionMenuMessage(chatId, "Yoh have not added any location", userId, false);
        }
        UserLocation userLocation = userLocationDAO.findByLocationForWeatherByDefault(userId);


        UserCurrentWeather currentWeather = weatherByLocationService.getCurrentWeather(userLocation.getUserLocality());
        userCurrentWeatherCash.saveCurrentWeather(userId, currentWeather);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(currentWeather.getBriefCurrentWeather());
        sendMessage.setParseMode(ParseMode.MARKDOWN);
        sendMessage.setReplyMarkup(menuService.getInlineKeyboardForCurrentWeather("Details", currentWeather.getHash()));
        telegramBot.execute(sendMessage);

        SendMessage sendMessage1 = new SendMessage();
        sendMessage1.setChatId(String.valueOf(chatId));
        sendMessage1.setText("Weather menu\uD83E\uDDED");
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
        UserCurrentWeather currentWeather = weatherByLocationService.getCurrentWeather(location);
        userCurrentWeatherCash.saveCurrentWeather(userId, currentWeather);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(currentWeather.getBriefCurrentWeather());
        sendMessage.setParseMode(ParseMode.MARKDOWN);
        sendMessage.setReplyMarkup(menuService.getInlineKeyboardForCurrentWeather("Details", currentWeather.getHash()));
        return sendMessage;
    }

    @SneakyThrows
    public BotApiMethod<?> showDifStateStateInCurrentWeather(long hashCode, CallbackQuery buttonQuery) {
        long userId = buttonQuery.getFrom().getId();
        long chatId = buttonQuery.getMessage().getChatId();


        UserCurrentWeather userCurrentWeather = userCurrentWeatherCash.getUserCurrentWeatherByHash(userId, hashCode);
        EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup();
        TelegramBot telegramBot = ApplicationContextProvider.getApplicationContext().getBean(TelegramBot.class);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));

        editMessageReplyMarkup.setChatId(String.valueOf(chatId));
        editMessageReplyMarkup.setMessageId(buttonQuery.getMessage().getMessageId());
        if (userCurrentWeather == null) {
            editMessageReplyMarkup.setReplyMarkup(null);
            sendMessage.setText("Requested weather is no longer stored");
            telegramBot.execute(sendMessage);
            return editMessageReplyMarkup;
        }
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(String.valueOf(chatId));
        editMessageText.setMessageId(buttonQuery.getMessage().getMessageId());
        if (buttonQuery.getMessage().getReplyMarkup().getKeyboard().get(0).get(0).getText().equals("Details")) {
            editMessageReplyMarkup.setReplyMarkup(menuService.getInlineKeyboardForCurrentWeather("Brief", hashCode));
            editMessageText.setText(userCurrentWeather.getBriefCurrentWeather() + userCurrentWeather.getDetailCurrentWeather());
            editMessageText.setParseMode(ParseMode.MARKDOWN);
            telegramBot.execute(editMessageText);
            return editMessageReplyMarkup;
        } else {
            editMessageReplyMarkup.setReplyMarkup(menuService.getInlineKeyboardForCurrentWeather("Details", hashCode));
            editMessageText.setText(userCurrentWeather.getBriefCurrentWeather());
            editMessageText.setParseMode(ParseMode.MARKDOWN);
            telegramBot.execute(editMessageText);
            return editMessageReplyMarkup;
        }
    }

    public BotApiMethod<?> selectDefaultLocation(long chatId, long userId) {
        if (userLocationDAO.findByUserID(userId).isEmpty()) {
            botStateCash.saveBotState(chatId, BotState.ADD_LOCATION);
            return menuService.getPositionMenuMessage(chatId, "Yoh have not added any location", userId, false);
        }
        List<UserLocation> userLocations = userLocationDAO.findByUserID(userId);

        int indexForWeatherByDefault = 0;
        for (int i = 0; i < userLocations.size(); i++) {
            if (userLocations.get(i).getLocationForWeatherByDefault()) {
                indexForWeatherByDefault = i;
                break;
            }
        }
        if (indexForWeatherByDefault != 0) {
            UserLocation loc = userLocations.get(0);
            userLocations.set(0, userLocations.get(indexForWeatherByDefault));
            userLocations.set(indexForWeatherByDefault, loc);
        }
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText("Select location");
        sendMessage.setReplyMarkup(menuService.getInlineKeyboardForSelectLocation(userLocations));
        return sendMessage;
    }

    public BotApiMethod<?> saveDefaultLocation(long locationId, CallbackQuery buttonQuery) {
        long userId = buttonQuery.getFrom().getId();
        long chatId = buttonQuery.getMessage().getChatId();
        List<UserLocation> userLocationList = userLocationDAO.findByUserID(userId);
        if (userLocationList == null) {
            return null;
        }
        if (userLocationList.size() == 1) {
            return null;
        }
        UserLocation prevLocationByDefault = userLocationDAO.findByLocationForWeatherByDefault(userId);
        if (locationId == prevLocationByDefault.getLocationId()) {
            return null;
        } else {
            prevLocationByDefault.setLocationForWeatherByDefault(false);
            userLocationDAO.saveLocation(prevLocationByDefault);
            UserLocation currentLocationByDefault = userLocationDAO.findByLocationId(locationId);
            currentLocationByDefault.setLocationForWeatherByDefault(true);
            userLocationDAO.saveLocation(currentLocationByDefault);
        }
        userLocationList = userLocationDAO.findByUserID(userId);


        EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup();
        editMessageReplyMarkup.setChatId(String.valueOf(chatId));
        editMessageReplyMarkup.setMessageId(buttonQuery.getMessage().getMessageId());
        editMessageReplyMarkup.setReplyMarkup(menuService.getInlineKeyboardForSelectLocation(userLocationList));
        return editMessageReplyMarkup;
    }

    @SneakyThrows
    public BotApiMethod<?> forecastWeatherStartPage(long chatId, long userId) {
        TelegramBot telegramBot = ApplicationContextProvider.getApplicationContext().getBean(TelegramBot.class);
        if (userLocationDAO.findByUserID(userId).isEmpty()) {
            botStateCash.saveBotState(chatId, BotState.ADD_LOCATION);
            return menuService.getPositionMenuMessage(chatId, "Yoh have not added any location", userId, false);
        }
        UserLocation userLocation = userLocationDAO.findByLocationForWeatherByDefault(userId);
        UserForecastWeather userForecastWeather = weatherByLocationService.getForecastWeather(userLocation.getUserLocality());
        userForecastWeatherCash.saveForecastWeather(userId, userForecastWeather);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(userForecastWeather.getBriefCurrentWeather());
        sendMessage.setParseMode(ParseMode.MARKDOWN);
        sendMessage.setReplyMarkup(menuService.getInlineKeyboardForForecastWeather("Details", userForecastWeather.getHash()));
        telegramBot.execute(sendMessage);

        SendMessage sendMessage1 = new SendMessage();
        sendMessage1.setChatId(String.valueOf(chatId));
        sendMessage1.setText("Weather menu\uD83E\uDDED");
        sendMessage1.setReplyMarkup(menuService.getWeatherMenuKeyboard());
        return sendMessage1;

    }

    @SneakyThrows
    public BotApiMethod<?> showDifStateStateInForecatWeather(long forecastHash, CallbackQuery buttonQuery) {
        long userId = buttonQuery.getFrom().getId();
        long chatId = buttonQuery.getMessage().getChatId();

        UserForecastWeather userForecastWeather = userForecastWeatherCash.getUserForecastWeatherByHash(userId, forecastHash);
        EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup();
        TelegramBot telegramBot = ApplicationContextProvider.getApplicationContext().getBean(TelegramBot.class);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));

        editMessageReplyMarkup.setChatId(String.valueOf(chatId));
        editMessageReplyMarkup.setMessageId(buttonQuery.getMessage().getMessageId());
        if (userForecastWeather == null) {
            editMessageReplyMarkup.setReplyMarkup(null);
            sendMessage.setText("Requested weather is no longer stored");
            telegramBot.execute(sendMessage);
            return editMessageReplyMarkup;
        }
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(String.valueOf(chatId));
        editMessageText.setMessageId(buttonQuery.getMessage().getMessageId());

        if (buttonQuery.getMessage().getReplyMarkup().getKeyboard().get(0).get(0).getText().equals("Details")) {
            editMessageReplyMarkup.setReplyMarkup(menuService.getInlineKeyboardForForecastWeather("Brief", forecastHash));
            editMessageText.setText(userForecastWeather.getDetailCurrentWeather());
            editMessageText.setParseMode(ParseMode.MARKDOWN);
            telegramBot.execute(editMessageText);
            return editMessageReplyMarkup;
        } else {
            editMessageReplyMarkup.setReplyMarkup(menuService.getInlineKeyboardForForecastWeather("Details", forecastHash));
            editMessageText.setText(userForecastWeather.getBriefCurrentWeather());
            editMessageText.setParseMode(ParseMode.MARKDOWN);
            telegramBot.execute(editMessageText);
            return editMessageReplyMarkup;
        }
    }
}

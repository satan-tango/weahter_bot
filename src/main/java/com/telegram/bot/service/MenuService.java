package com.telegram.bot.service;

import com.telegram.bot.cash.UserLocationCash;
import com.telegram.bot.entity.UserLocation;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@AllArgsConstructor
public class MenuService {

    public SendMessage getMainMenuMessage(long chatId, String textMessage, long userId) {
        ReplyKeyboardMarkup replyKeyboardMarkup = getMainMenuKeyboard(userId);
        SendMessage sendMessage = new SendMessage();

        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(textMessage);
        sendMessage.setReplyMarkup(replyKeyboardMarkup);

        return sendMessage;
    }

    public SendMessage getSettingsMenuMessage(long chatId, String textMessage, long userId) {
        ReplyKeyboardMarkup replyKeyboardMarkup = getSettingsMenuKeyboard(userId);
        SendMessage sendMessage = new SendMessage();

        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(textMessage);
        sendMessage.setReplyMarkup(replyKeyboardMarkup);

        return sendMessage;
    }

    public SendMessage getLocationMenuMessage(long chatId, String textMessage, long userId) {
        ReplyKeyboardMarkup replyKeyboardMarkup = getLocationMenuKeyboard(userId);
        SendMessage sendMessage = new SendMessage();

        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(textMessage);
        sendMessage.setReplyMarkup(replyKeyboardMarkup);

        return sendMessage;
    }

    public SendMessage getPositionMenuMessage(long chatId, String textMessage, long userId, boolean isNewUSer) {
        ReplyKeyboardMarkup replyKeyboardMarkup = getPositionMenuKeyboard(userId, isNewUSer);
        SendMessage sendMessage = new SendMessage();

        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(textMessage);
        sendMessage.setReplyMarkup(replyKeyboardMarkup);

        return sendMessage;
    }


    public SendMessage getInlineMessageForDeleteLocation(long chatId, List<UserLocation> userLocations) {
        InlineKeyboardMarkup inlineKeyboardMarkup = getInlineKeyboardForDeleteLocation(userLocations);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText("Click on location which you want to delete");
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);
        return sendMessage;
    }


    private ReplyKeyboardMarkup getLocationMenuKeyboard(long userId) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        KeyboardRow row2 = new KeyboardRow();
        row1.add(new KeyboardButton("Add location"));
        row1.add(new KeyboardButton("Delete location"));
        row2.add(new KeyboardButton("\uD83D\uDD19 Back"));
        keyboard.add(row1);
        keyboard.add(row2);
        replyKeyboardMarkup.setKeyboard(keyboard);

        return replyKeyboardMarkup;
    }

    private ReplyKeyboardMarkup getSettingsMenuKeyboard(long userId) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        KeyboardRow row2 = new KeyboardRow();
        row1.add(new KeyboardButton("Locations"));
        row1.add(new KeyboardButton("Language"));
        row2.add(new KeyboardButton("\uD83D\uDD19 Back"));
        keyboard.add(row1);
        keyboard.add(row2);
        replyKeyboardMarkup.setKeyboard(keyboard);

        return replyKeyboardMarkup;
    }

    private ReplyKeyboardMarkup getPositionMenuKeyboard(long userId, boolean isNewUser) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardButton keyboardButton = new KeyboardButton();
        if (isNewUser) {
            KeyboardRow row1 = new KeyboardRow();
            keyboardButton.setText("Location");
            row1.add(keyboardButton);
            keyboard.add(row1);
        } else {
            KeyboardRow row1 = new KeyboardRow();
            KeyboardRow row2 = new KeyboardRow();
            keyboardButton.setText("Location");
            row2.add(new KeyboardButton("\uD83D\uDD19 Back"));
            keyboardButton.setRequestLocation(true);
            row1.add(keyboardButton);
            keyboard.add(row1);
            keyboard.add(row2);
        }

        replyKeyboardMarkup.setKeyboard(keyboard);

        return replyKeyboardMarkup;
    }

    private ReplyKeyboardMarkup getMainMenuKeyboard(long userId) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        KeyboardRow row2 = new KeyboardRow();
        KeyboardButton keyboardButton = new KeyboardButton();

        row1.add(new KeyboardButton("Weather"));
        row1.add(new KeyboardButton("Forecast"));
        row2.add(new KeyboardButton("Support"));
        row2.add(new KeyboardButton("Settings"));
        keyboard.add(row1);
        keyboard.add(row2);
        replyKeyboardMarkup.setKeyboard(keyboard);

        return replyKeyboardMarkup;
    }

    public InlineKeyboardMarkup getInlineKeyboardForDeleteLocation(List<UserLocation> userLocations) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        for (UserLocation location : userLocations) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText("\uD83D\uDCDB " + location.getUserCountry() + ", "
                    + location.getUserRegion() + " Region, " + location.getUserLocality());
            button.setCallbackData("delete:" + location.getLocationId());
            buttons.add(Arrays.asList(button));
        }
        inlineKeyboardMarkup.setKeyboard(buttons);
        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getInlineKeyboardForWeather(List<UserLocation> userLocations) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();

        for (UserLocation location : userLocations) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText("\uD83D\uDCCC " + location.getUserCountry() + ", "
                    + location.getUserRegion() + " Region, " + location.getUserLocality());
            button.setCallbackData("weather:" + location.getLocationId());
            buttons.add(Arrays.asList(button));
        }
        inlineKeyboardMarkup.setKeyboard(buttons);
        return inlineKeyboardMarkup;
    }

    public ReplyKeyboardMarkup getWeatherMenuKeyboard() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        KeyboardRow row2 = new KeyboardRow();
        KeyboardRow row3 = new KeyboardRow();
        row1.add(new KeyboardButton("Select location"));
        row2.add(new KeyboardButton("Add location"));
        row3.add(new KeyboardButton("\uD83D\uDD19 Back"));
        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);
        replyKeyboardMarkup.setKeyboard(keyboard);

        return replyKeyboardMarkup;
    }

    public InlineKeyboardMarkup getInlineKeyboardForAddLocation(List<UserLocation> userLocations, int key) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        for (int i = 0; i < userLocations.size(); i++) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText("✅ " + userLocations.get(i).getUserCountry() + ", "
                    + userLocations.get(i).getUserRegion() + " Region"
                    + ", " + userLocations.get(i).getUserLocality());
            button.setCallbackData("add:" + key + "," + i);
            buttons.add(Arrays.asList(button));
        }
        inlineKeyboardMarkup.setKeyboard(buttons);
        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getInlineKeyboardForCurrentWeather(String text, long currentWeatherHash) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData("currentWeather:" + currentWeatherHash);
        buttons.add(Arrays.asList(button));
        inlineKeyboardMarkup.setKeyboard(buttons);
        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getInlineKeyboardForForecastWeather(String text, long forecastWeatherHash) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData("forecastWeather:" + forecastWeatherHash);
        buttons.add(Arrays.asList(button));
        inlineKeyboardMarkup.setKeyboard(buttons);
        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getInlineKeyboardForSelectLocation(List<UserLocation> list) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            if (list.get(i).getLocationForWeatherByDefault() == true) {
                button.setText("✅ " + list.get(i).getUserCountry() + ", "
                        + list.get(i).getUserRegion() + " Region"
                        + ", " + list.get(i).getUserLocality());
            } else {
                button.setText(list.get(i).getUserCountry() + ", "
                        + list.get(i).getUserRegion() + " Region"
                        + ", " + list.get(i).getUserLocality());
            }
            button.setCallbackData("select:" + list.get(i).getLocationId());
            buttons.add(Arrays.asList(button));
        }
        inlineKeyboardMarkup.setKeyboard(buttons);
        return inlineKeyboardMarkup;
    }
}

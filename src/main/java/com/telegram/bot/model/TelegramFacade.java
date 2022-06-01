package com.telegram.bot.model;

import com.telegram.bot.cash.BotStateCash;
import com.telegram.bot.model.handler.CallbackQueryHandler;
import com.telegram.bot.model.handler.MessageHandler;
import lombok.AllArgsConstructor;
import org.hibernate.pretty.MessageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@AllArgsConstructor
public class TelegramFacade {

    private final BotStateCash botStateCash;

    private final MessageHandler messageHandler;

    private final CallbackQueryHandler callbackQueryHandler;

    public BotApiMethod<?> handleUpdate(Update update) {
        if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            return callbackQueryHandler.processCallbackQuery(callbackQuery);
        } else {
            Message message = update.getMessage();
            if (message != null && message.hasText()) {
                return handleInputMessage(message);
            }
            if (message != null && message.hasLocation()) {
                return messageHandler.handle(message, BotState.LOCATION_BY_GPS);
            }
        }
        return null;
    }

    private BotApiMethod<?> handleInputMessage(Message message) {
        BotState botState;
        String inputMsg = message.getText();

        if (botStateCash.getBotStateMap().get(message.getChatId()) == BotState.ADD_LOCATION) {
            botState = BotState.LOCATION_BY_CHAT;
            return messageHandler.handle(message, botState);
        }

        switch (inputMsg) {
            case "/start":
                botState = BotState.START;
                break;
            case "Weather":
                botState = BotState.WEATHER;
                break;
            case "Forecast":
                botState = BotState.FORECAST;
                break;
            case ("Settings"):
                botState = BotState.SETTINGS;
                break;
            case ("Locations"):
                botState = BotState.LOCATIONS;
                break;
            case ("Add location"):
                botState = BotState.ADD_LOCATION;
                break;
            case ("Delete location"):
                botState = BotState.DELETE_LOCATION;
                break;
            case ("Location"):
                botState = BotState.LOCATION_BY_GPS;
                break;
            default:
                if (botStateCash.getBotStateMap().get(message.getChatId()) == null) {
                    botState = BotState.START;
                } else {
                    botState = botStateCash.getBotStateMap().get(message.getChatId());
                }
        }
        return messageHandler.handle(message, botState);
    }
}

package com.telegram.bot.model;

import com.telegram.bot.cash.BotStateCash;
import com.telegram.bot.model.handler.CallbackQueryHandler;
import com.telegram.bot.model.handler.MessageHandler;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;


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
                return messageHandler.handle(message, BotState.LOCATION_BY_COORDINATE);
            }
        }
        return null;
    }

    private BotApiMethod<?> handleInputMessage(Message message) {
        BotState botState;
        String inputMsg = message.getText();
        long chatId = message.getChatId();

        if (botStateCash.getBotStateMap().get(chatId) != null) {
            if (botStateCash.getCurrentBotState(chatId) == BotState.ADD_LOCATION
                    && !inputMsg.equals("/start") && !inputMsg.equals("\uD83D\uDD19 Back")) {
                botState = BotState.LOCATION_BY_CHAT;
                return messageHandler.handle(message, botState);
            }
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
                botState = BotState.LOCATION_BY_COORDINATE;
                break;
            case ("\uD83D\uDD19 Back"):
                botState = botStateCash.getPreviousBotState(chatId);
                break;
            default:
                if (botStateCash.getBotStateMap().get(chatId) == null) {
                    botState = BotState.START;
                } else {
                    botState = botStateCash.getCurrentBotState(chatId);
                }
        }
        return messageHandler.handle(message, botState);
    }
}

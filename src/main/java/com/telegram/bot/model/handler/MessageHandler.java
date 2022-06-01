package com.telegram.bot.model.handler;

import com.telegram.bot.DAO.UserDAO;
import com.telegram.bot.cash.BotStateCash;
import com.telegram.bot.model.BotState;
import com.telegram.bot.service.MenuService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
@AllArgsConstructor
public class MessageHandler {

    private final UserDAO userDAO;

    private final EventHandler eventHandler;

    private final BotStateCash botStateCash;

    private final MenuService menuService;


    public BotApiMethod<?> handle(Message message, BotState botState) {
        long userId = message.getFrom().getId();
        long chatId = message.getChatId();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));

        if (!userDAO.isExist(userId)) {
            eventHandler.saveNewUser(message, userId);
            botState = BotState.ADD_LOCATION;
        }

        botStateCash.saveBotState(userId, botState);

        switch (botState.name()) {
            case ("START"):
                return menuService.getMainMenuMessage(chatId, "Hello,\n" +
                        "I am the weather bot and i can inform\n" +
                        "about the weather. Select an action\n" +
                        "from the menu.", userId);
            case ("SETTINGS"):
                return menuService.getSettingsMenuMessage(chatId, "Settings menu", userId);
            case ("LOCATIONS"):
                return menuService.getLocationMenuMessage(chatId, "Location menu", userId);
            case "ADD_LOCATION":
                return menuService.getPositionMenuMessage(chatId, "Press button to share your location\n" +
                        "or send the name of location", userId);
            case "LOCATION_BY_GPS":
                return eventHandler.saveLocationByGPS(chatId, userId, message);
            case "LOCATION_BY_CHAT":
                return eventHandler.saveLocationByChat(chatId, userId, message);
            case "DELETE_LOCATION":
                return eventHandler.queryLocationButtonToDelete(chatId, userId);
            default:
                return null;
        }
    }
}

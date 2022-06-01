package com.telegram.bot.model.handler;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

@Component
@AllArgsConstructor
public class CallbackQueryHandler {

    private final EventHandler eventHandler;

    public BotApiMethod<?> processCallbackQuery(CallbackQuery buttonQuery) {
        long chatId = buttonQuery.getMessage().getChatId();
        long userId = buttonQuery.getFrom().getId();

        BotApiMethod<?> callbackAnswer = null;

        String data = buttonQuery.getData();
        long locationId = 1;
        if (data.contains("delete")) {
            String[] deleteLocation = data.split(":");
            data = deleteLocation[0];
            locationId = Long.parseLong(deleteLocation[1]);
        }
        switch (data) {
            case "delete":
                return eventHandler.deleteLocation(locationId, buttonQuery);
            default:
                return null;
        }
    }
}

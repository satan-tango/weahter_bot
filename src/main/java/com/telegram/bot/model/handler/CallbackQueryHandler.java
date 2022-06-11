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

        String callBack = buttonQuery.getData();
        String[] arr = callBack.split(":");
        String callbackCommand = arr[0];
        String data = arr[1];
        switch (callbackCommand) {
            case "delete":
                return eventHandler.deleteLocation(Long.parseLong(data), buttonQuery);
            case "add":
                return eventHandler.saveLocation(Integer.parseInt(data), buttonQuery);
            case "weather":
                return eventHandler.showWeather(Long.parseLong(data), buttonQuery);
            default:
                return null;
        }
    }
}

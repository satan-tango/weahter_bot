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
                String[] dataArr = data.split(",");
                int key = Integer.parseInt(dataArr[0]);
                int index = Integer.parseInt(dataArr[1]);
                return eventHandler.saveLocation(key, index, buttonQuery);
            case "weather":
                return eventHandler.showWeather(Long.parseLong(data), buttonQuery);
            case "currentWeather":
                return eventHandler.showDifStateStateInCurrentWeather(Long.parseLong(data), buttonQuery);
            case "forecastWeather":
                return eventHandler.showDifStateStateInForecatWeather(Long.parseLong(data), buttonQuery);
            case "select":
                return eventHandler.saveDefaultLocation(Long.parseLong(data), buttonQuery);
            default:
                return null;
        }
    }
}

package com.telegram.bot.service;

import com.telegram.bot.botconfig.TelegramBotConfig;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@Service
@AllArgsConstructor
public class RequestToBotService {

    private final TelegramBotConfig telegramBotConfig;

    @SneakyThrows
    public void SendTextToBot(String text, long chatId) {
        final String APIUrl = "https://api.telegram.org/bot";
        URL url = new URL(APIUrl + telegramBotConfig.getBotToken() +
                "/sendMessage");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");

        byte[] out = "{\"chat_id\":\"}".getBytes(StandardCharsets.UTF_8);
    }
}

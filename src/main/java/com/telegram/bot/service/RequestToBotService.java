package com.telegram.bot.service;

import com.telegram.bot.botconfig.TelegramBotConfig;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
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
        con.setRequestProperty("Content-type", "application/json; utf-8");
        con.setDoOutput(true);
        con.setConnectTimeout(1000);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("chat_id", chatId);
        jsonObject.put("text", text);
        byte[] out = jsonObject.toString().getBytes(StandardCharsets.UTF_8);

        OutputStream os = con.getOutputStream();
        os.write(out);
        os.close();
        BufferedReader br = new BufferedReader
                (new InputStreamReader(con.getInputStream(), "utf-8"));
        StringBuilder response = new StringBuilder();
        String responseLine = null;
        while ((responseLine = br.readLine()) != null) {
            response.append(responseLine.trim());
        }
        br.close();
    }
}

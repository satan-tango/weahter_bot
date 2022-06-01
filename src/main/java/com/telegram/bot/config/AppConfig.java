package com.telegram.bot.config;

import com.telegram.bot.botconfig.TelegramBotConfig;
import com.telegram.bot.model.TelegramBot;
import com.telegram.bot.model.TelegramFacade;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;

@Configuration
@AllArgsConstructor
public class AppConfig {

    private final TelegramBotConfig telegramBotConfig;

    @Bean
    public SetWebhook setWebhookInstance() {
        return SetWebhook.builder().url(telegramBotConfig.getWebHookPath()).build();
    }

    @Bean
    public TelegramBot springWebhookBot(SetWebhook setWebhook, TelegramFacade telegramFacade) {
        TelegramBot bot = new TelegramBot(setWebhook, telegramFacade);
        bot.setBotToken(telegramBotConfig.getBotToken());
        bot.setBotUsername(telegramBotConfig.getUserName());
        bot.setBotPath(telegramBotConfig.getWebHookPath());

        return bot;
    }
}

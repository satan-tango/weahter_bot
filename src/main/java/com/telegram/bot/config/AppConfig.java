package com.telegram.bot.config;

import com.telegram.bot.botconfig.TelegramBotConfig;
import com.telegram.bot.model.TelegramBot;
import com.telegram.bot.model.TelegramFacade;
import lombok.AllArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;

import java.util.Locale;

@Configuration
@AllArgsConstructor
public class AppConfig {

    private final TelegramBotConfig telegramBotConfig;

    @Bean
    public SetWebhook setWebhookInstance() {
        return SetWebhook.builder().url(telegramBotConfig.getWebHookPath()).build();
    }

    @Bean
    public MessageSource messageSourceInstance() {
        ReloadableResourceBundleMessageSource messageSource
                = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:emoji_by_code");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
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

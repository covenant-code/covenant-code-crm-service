package com.covenantcode.crm.telegram.manager.config;

import com.covenantcode.crm.telegram.manager.service.TelegramNotificationService;
import com.covenantcode.crm.telegram.manager.service.impl.NoOpTelegramNotificationServiceImpl;
import com.covenantcode.crm.telegram.manager.service.impl.TelegramNotificationServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.generics.TelegramClient;


@Slf4j
@Configuration
@RequiredArgsConstructor
public class TelegramConfig {

    private final TelegramProperties telegramProperties;

    @Bean
    @ConditionalOnProperty(name = "telegram.enabled", havingValue = "true")
    public TelegramClient telegramClient() {
        log.info("Initializing Telegram client with bot token");
        return new OkHttpTelegramClient(telegramProperties.getBotToken());
    }

    @Bean
    @ConditionalOnProperty(name = "telegram.enabled", havingValue = "true")
    public TelegramNotificationService telegramNotificationService(TelegramClient telegramClient) {
        return new TelegramNotificationServiceImpl(telegramClient, telegramProperties);
    }

    @Bean
    @ConditionalOnProperty(name = "telegram.enabled", havingValue = "false", matchIfMissing = true)
    public TelegramNotificationService noOpTelegramNotificationService() {
        log.info("Telegram is disabled, using NoOp implementation");
        return new NoOpTelegramNotificationServiceImpl();
    }
}

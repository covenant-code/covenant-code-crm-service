package com.covenantcode.crm.telegram.lead;


import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "telegram.lead-bot", name = "enabled", havingValue = "true")
public class LeadTelegramBotRegister {

    private final TelegramBotsLongPollingApplication telegramBotsApplication;

    private final LeadTelegramBot leadTelegramBot;

    private final LeadBotProperties properties;

    @PostConstruct
    public void registerBot() {
        if (properties.getToken() == null || properties.getToken().isBlank()) {
            log.warn("Lead Telegram bot is enabled, but token is empty. Bot will not be registered.");
            return;
        }

        try {
            telegramBotsApplication.registerBot(properties.getToken(), leadTelegramBot);
            log.info("Lead Telegram bot registered successfully. Username: {}", properties.getUsername());
        } catch (Exception e) {
            log.error("Failed to register Lead Telegram bot", e);
        }
    }
}

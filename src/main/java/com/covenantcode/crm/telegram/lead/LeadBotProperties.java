package com.covenantcode.crm.telegram.lead;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "telegram.lead-bot")
public class LeadBotProperties {

    /**
     * Токен публичного Telegram-бота от @BotFather.
     */
    private String token;

    /**
     * Username бота без @.
     * Например: covenant_code_lead_bot
     */
    private String username;

    /**
     * Флаг включения публичного lead-бота.
     */
    private boolean enabled;
}

package com.covenantcode.crm.service.impl;

import com.covenantcode.crm.entity.Lead;
import com.covenantcode.crm.entity.enums.LeadStatus;
import com.covenantcode.crm.telegram.manager.config.TelegramProperties;
import com.covenantcode.crm.telegram.manager.service.impl.TelegramNotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TelegramNotificationServiceImplTest {

    @Mock
    private TelegramClient telegramClient;

    @Mock
    private TelegramProperties telegramProperties;

    private TelegramNotificationServiceImpl telegramNotificationService;

    @BeforeEach
    void setUp() {
        telegramNotificationService = new TelegramNotificationServiceImpl(
                telegramClient, telegramProperties
        );
    }

    private Lead sampleLead() {
        Lead lead = new Lead();
        lead.setFirstName("Иван");
        lead.setLastName("Петров");
        lead.setPhone("+79161234567");
        lead.setEmail("ivan@example.com");
        lead.setStatus(LeadStatus.NEW);
        lead.setCreatedAt(OffsetDateTime.now());
        return lead;
    }

    @Test
    void notifyLeadCreated_defaultChatIdConfigured_messageSentOnce() throws TelegramApiException {
        when(telegramProperties.getDefaultChatId()).thenReturn("489224478");

        telegramNotificationService.notifyLeadCreated(sampleLead());

        verify(telegramClient, times(1)).execute(any(SendMessage.class));
    }

    @Test
    void notifyLeadCreated_telegramApiThrows_doesNotPropagateException() throws TelegramApiException {
        when(telegramProperties.getDefaultChatId()).thenReturn("489224478");
        when(telegramClient.execute(any(SendMessage.class)))
                .thenThrow(new TelegramApiException("Telegram is unreachable"));

        assertThatCode(() -> telegramNotificationService.notifyLeadCreated(sampleLead()))
                .doesNotThrowAnyException();

        verify(telegramClient, times(1)).execute(any(SendMessage.class));
    }

    @Test
    void notifyLeadCreated_noDefaultChatIdConfigured_nothingSent() throws TelegramApiException {
        when(telegramProperties.getDefaultChatId()).thenReturn(null);

        telegramNotificationService.notifyLeadCreated(sampleLead());

        verify(telegramClient, never()).execute(any(SendMessage.class));
    }
}

package com.covenantcode.crm.service.impl;

import com.covenantcode.crm.config.TelegramProperties;
import com.covenantcode.crm.entity.Lead;
import com.covenantcode.crm.entity.User;
import com.covenantcode.crm.entity.enums.LeadStatus;
import com.covenantcode.crm.entity.enums.RoleName;
import com.covenantcode.crm.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.OffsetDateTime;
import java.util.List;

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

    @Mock
    private UserRepository userRepository;

    private TelegramNotificationServiceImpl telegramNotificationService;

    @BeforeEach
    void setUp() {
        telegramNotificationService = new TelegramNotificationServiceImpl(
                telegramClient, telegramProperties, userRepository
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

    private User managerWithChatId(String chatId) {
        return User.builder()
                .id(1L)
                .firstName("Manager")
                .lastName("Managerov")
                .email("manager@test.ru")
                .telegramChatId(chatId)
                .build();
    }

    @Test
    void notifyLeadCreated_managerHasChatId_messageSentOnce() throws TelegramApiException {
        when(userRepository.findAllByRole_NameInAndTelegramChatIdIsNotNull(
                List.of(RoleName.ADMIN, RoleName.MANAGER)))
                .thenReturn(List.of(managerWithChatId("123")));
        when(telegramProperties.isEnabled()).thenReturn(true);

        telegramNotificationService.notifyLeadCreated(sampleLead());

        verify(telegramClient, times(1)).execute(any(SendMessage.class));
    }

    @Test
    void notifyLeadCreated_telegramApiThrows_doesNotPropagateException() throws TelegramApiException {
        when(userRepository.findAllByRole_NameInAndTelegramChatIdIsNotNull(
                List.of(RoleName.ADMIN, RoleName.MANAGER)))
                .thenReturn(List.of(managerWithChatId("123")));
        when(telegramProperties.isEnabled()).thenReturn(true);
        when(telegramClient.execute(any(SendMessage.class)))
                .thenThrow(new TelegramApiException("Telegram is unreachable"));

        assertThatCode(() -> telegramNotificationService.notifyLeadCreated(sampleLead()))
                .doesNotThrowAnyException();

        verify(telegramClient, times(1)).execute(any(SendMessage.class));
    }

    @Test
    void notifyLeadCreated_noRecipientsAndNoDefaultChatId_nothingSent() throws TelegramApiException {
        when(userRepository.findAllByRole_NameInAndTelegramChatIdIsNotNull(
                List.of(RoleName.ADMIN, RoleName.MANAGER)))
                .thenReturn(List.of());
        when(telegramProperties.getDefaultChatId()).thenReturn(null);

        telegramNotificationService.notifyLeadCreated(sampleLead());

        verify(telegramClient, never()).execute(any(SendMessage.class));
    }
}

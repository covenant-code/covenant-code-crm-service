package com.covenantcode.telegram;

import com.covenantcode.crm.dto.lead.LeadCreateRequest;
import com.covenantcode.crm.entity.Course;
import com.covenantcode.crm.service.LeadService;
import com.covenantcode.crm.telegram.lead.BotContentService;
import com.covenantcode.crm.telegram.lead.BotStep;
import com.covenantcode.crm.telegram.lead.LeadBotSession;
import com.covenantcode.crm.telegram.lead.LeadTelegramBot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.atLeastOnce;


@ExtendWith(MockitoExtension.class)
class LeadTelegramBotTest {

    private static final Long CHAT_ID = 123456789L;

    @Mock
    private LeadService leadService;

    @Mock
    private BotContentService botContentService;

    @Mock
    private TelegramClient telegramClient;

    private LeadTelegramBot bot;

    @BeforeEach
    void setUp() throws Exception {
        bot = new LeadTelegramBot(
                leadService,
                botContentService,
                telegramClient
        );

        when(telegramClient.execute(any(SendMessage.class)))
                .thenReturn(null);
    }

    @Test
    @DisplayName("Команда /apply запускает диалог создания заявки и создаёт сессию")
    void consume_applyCommand_shouldStartLeadDialogAndCreateSession() throws Exception {
        Update update = textUpdate("/apply");

        bot.consume(update);

        Map<Long, LeadBotSession> sessions = getSessions();

        assertThat(sessions).containsKey(CHAT_ID);
        assertThat(sessions.get(CHAT_ID).getStep()).isEqualTo(BotStep.AWAITING_FIRST_NAME);

        ArgumentCaptor<SendMessage> messageCaptor = ArgumentCaptor.forClass(SendMessage.class);

        verify(telegramClient).execute(messageCaptor.capture());

        SendMessage sentMessage = messageCaptor.getValue();

        assertThat(sentMessage.getChatId()).isEqualTo(CHAT_ID.toString());
        assertThat(sentMessage.getText()).contains("Как вас зовут");
    }

    @Test
    @DisplayName("Полный диалог с выбором курса создаёт заявку и удаляет сессию")
    void consume_fullDialog_withCourse_shouldCreateLeadAndRemoveSession() throws Exception {
        Course course = new Course();
        course.setId(1L);
        course.setTitle("Java Backend");

        when(botContentService.getActiveCourseNames())
                .thenReturn(List.of("Java Backend"));

        when(botContentService.findCourseByTitle("Java Backend"))
                .thenReturn(Optional.of(course));

        bot.consume(textUpdate("/start"));
        bot.consume(textUpdate("Оставить заявку"));

        bot.consume(textUpdate("Иван"));
        bot.consume(textUpdate("Иванов"));
        bot.consume(textUpdate("+79991234567"));
        bot.consume(textUpdate("Java Backend"));

        ArgumentCaptor<LeadCreateRequest> captor =
                ArgumentCaptor.forClass(LeadCreateRequest.class);

        verify(leadService).create(captor.capture());

        LeadCreateRequest request = captor.getValue();

        assertEquals("Иван", request.getFirstName());
        assertEquals("Иванов", request.getLastName());
        assertEquals("+79991234567", request.getPhone());
        assertEquals(1L, request.getInterestedCourseId());
        assertEquals("TELEGRAM_BOT", request.getSource());

        Map<Long, LeadBotSession> sessions = getSessions();

        assertThat(sessions).doesNotContainKey(CHAT_ID);
    }

    @Test
    @DisplayName("Команда /cancel во время диалога удаляет сессию и не создаёт заявку")
    void consume_cancelDuringDialog_shouldRemoveSessionAndNotCreateLead() throws Exception {
        LeadBotSession session = LeadBotSession.builder()
                .chatId(CHAT_ID)
                .step(BotStep.AWAITING_LAST_NAME)
                .firstName("Иван")
                .build();

        getSessions().put(CHAT_ID, session);

        bot.consume(textUpdate("/cancel"));

        Map<Long, LeadBotSession> sessions = getSessions();

        assertThat(sessions).doesNotContainKey(CHAT_ID);

        verify(leadService, never()).create(any());

        ArgumentCaptor<SendMessage> messageCaptor = ArgumentCaptor.forClass(SendMessage.class);

        verify(telegramClient).execute(messageCaptor.capture());

        SendMessage sentMessage = messageCaptor.getValue();

        assertThat(sentMessage.getText()).contains("Текущий диалог отменён");
    }

    @Test
    @DisplayName("Ошибка при создании заявки отправляет сообщение об ошибке и не прерывает работу бота")
    void consume_createLeadThrowsException_shouldSendErrorMessageAndNotThrowException() throws Exception {
        Course course = new Course();
        course.setId(1L);
        course.setTitle("Java Backend");

        when(botContentService.getActiveCourseNames())
                .thenReturn(List.of("Java Backend"));

        when(botContentService.findCourseByTitle("Java Backend"))
                .thenReturn(Optional.of(course));

        doThrow(new RuntimeException("Database is unavailable"))
                .when(leadService)
                .create(any(LeadCreateRequest.class));

        assertThatNoException().isThrownBy(() -> {
            bot.consume(textUpdate("/start"));
            bot.consume(textUpdate("Оставить заявку"));

            bot.consume(textUpdate("Иван"));
            bot.consume(textUpdate("Петров"));
            bot.consume(textUpdate("+79161234567"));
            bot.consume(textUpdate("Java Backend"));
        });

        verify(leadService, times(1)).create(any(LeadCreateRequest.class));

        ArgumentCaptor<SendMessage> messageCaptor =
                ArgumentCaptor.forClass(SendMessage.class);

        verify(telegramClient, atLeastOnce()).execute(messageCaptor.capture());

        assertThat(messageCaptor.getAllValues())
                .extracting(SendMessage::getText)
                .anyMatch(text -> text.contains("Не удалось создать заявку"));
    }

    @SuppressWarnings("unchecked")
    private Map<Long, LeadBotSession> getSessions() {
        return (Map<Long, LeadBotSession>) ReflectionTestUtils.getField(bot, "sessions");
    }

    private Update textUpdate(String text) {
        Chat chat = Chat.builder()
                .id(CHAT_ID)
                .type("private")
                .build();

        Message message = Message.builder()
                .text(text)
                .chat(chat)
                .build();

        Update update = new Update();
        update.setMessage(message);

        return update;
    }
}
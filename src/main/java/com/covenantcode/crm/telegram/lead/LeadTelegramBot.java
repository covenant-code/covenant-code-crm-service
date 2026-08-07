package com.covenantcode.crm.telegram.lead;

import com.covenantcode.crm.dto.lead.LeadCreateRequest;
import com.covenantcode.crm.entity.Course;
import com.covenantcode.crm.service.LeadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.generics.TelegramClient;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "telegram.lead-bot", name = "enabled", havingValue = "true")
public class LeadTelegramBot implements LongPollingSingleThreadUpdateConsumer {

    private static final String BUTTON_TIP = "📚 Совет дня";
    private static final String BUTTON_COURSES = "🎓 Наши курсы";
    private static final String BUTTON_APPLY = "📝 Оставить заявку";
    private static final String BUTTON_FAQ = "❓ FAQ";
    private static final String BUTTON_MAIN_MENU = "Главное меню";

    private static final String SOURCE_TELEGRAM_BOT = "TELEGRAM_BOT";

    private final LeadService leadService;
    private final BotContentService botContentService;
    private final TelegramClient telegramClient;
    private final Map<Long, LeadBotSession> sessions = new ConcurrentHashMap<>();

    @Autowired
    public LeadTelegramBot(
            LeadService leadService,
            BotContentService botContentService,
            LeadBotProperties properties
    ) {
        this(
                leadService,
                botContentService,
                new OkHttpTelegramClient(properties.getToken())
        );
    }

    public LeadTelegramBot(
            LeadService leadService,
            BotContentService botContentService,
            TelegramClient telegramClient
    ) {
        this.leadService = leadService;
        this.botContentService = botContentService;
        this.telegramClient = telegramClient;
    }

    @Override
    public void consume(Update update) {
        if (update == null || !update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }

        Long chatId = update.getMessage().getChatId();
        String text = normalizeText(update.getMessage().getText());

        log.debug("Lead bot received message. chatId={}, text={}", chatId, text);

        LeadBotSession session = sessions.getOrDefault(
                chatId,
                LeadBotSession.builder()
                        .chatId(chatId)
                        .step(BotStep.IDLE)
                        .build()
        );

        try {
            handleTextMessage(chatId, text, session);
        } catch (Exception e) {
            log.error("Error while handling lead bot message. chatId={}, text={}", chatId, text, e);
            sendMessage(chatId, "Произошла ошибка. Попробуйте позже или напишите нам напрямую.");
        }
    }

    private void handleTextMessage(Long chatId, String text, LeadBotSession session) {
        if (isStartCommand(text) || isMainMenuCommand(text)) {
            sessions.remove(chatId);
            showMainMenu(chatId);
            return;
        }

        if (isCancelCommand(text)) {
            cancelDialog(chatId);
            return;
        }

        if (isTipCommand(text)) {
            sendRandomTip(chatId);
            return;
        }

        if (isCoursesCommand(text)) {
            sendCourseList(chatId);
            return;
        }

        if (isFaqCommand(text)) {
            sendFaq(chatId);
            return;
        }

        if (isApplyCommand(text)) {
            startLeadDialog(chatId);
            return;
        }

        if (session.getStep() != BotStep.IDLE) {
            handleDialogStep(chatId, text, session);
            return;
        }

        sendUnknownCommand(chatId);
    }

    private void showMainMenu(Long chatId) {
        String message = """
                👋 Привет! Я бот учебного центра Covenant Code.
                
                Здесь ты найдёшь:
                • 💡 полезные советы для начинающих разработчиков
                • 🎓 информацию о наших курсах
                • 📝 возможность оставить заявку на обучение
                
                Выбери действие в меню ниже.
                """;

        sendMessageWithMainKeyboard(chatId, message);
    }

    private void sendRandomTip(Long chatId) {
        sendMessage(chatId, botContentService.getRandomTip());
    }

    private void sendCourseList(Long chatId) {
        sendMessage(chatId, botContentService.formatCourses());
    }

    private void sendFaq(Long chatId) {
        sendMessage(chatId, botContentService.getFaq());
    }

    private void startLeadDialog(Long chatId) {
        LeadBotSession session = LeadBotSession.builder()
                .chatId(chatId)
                .step(BotStep.AWAITING_FIRST_NAME)
                .build();

        sessions.put(chatId, session);

        sendMessageWithRemoveKeyboard(
                chatId,
                """
                Отлично! Давайте оформим заявку.
                
                Как вас зовут? Введите имя:
                """
        );
    }

    private void handleDialogStep(Long chatId, String text, LeadBotSession session) {
        switch (session.getStep()) {
            case AWAITING_FIRST_NAME -> handleFirstName(chatId, text, session);
            case AWAITING_LAST_NAME -> handleLastName(chatId, text, session);
            case AWAITING_PHONE -> handlePhone(chatId, text, session);
            case AWAITING_COURSE -> handleCourse(chatId, text, session);
            case IDLE -> sendUnknownCommand(chatId);
        }
    }

    private void handleFirstName(Long chatId, String text, LeadBotSession session) {
        if (isInvalidName(text)) {
            sendMessage(chatId, "Имя должно содержать минимум 2 символа. Введите имя ещё раз:");
            return;
        }

        session.setFirstName(text);
        session.setStep(BotStep.AWAITING_LAST_NAME);
        sessions.put(chatId, session);

        sendMessage(chatId, "Введите фамилию:");
    }

    private void handleLastName(Long chatId, String text, LeadBotSession session) {
        if (isInvalidName(text)) {
            sendMessage(chatId, "Фамилия должна содержать минимум 2 символа. Введите фамилию ещё раз:");
            return;
        }

        session.setLastName(text);
        session.setStep(BotStep.AWAITING_PHONE);
        sessions.put(chatId, session);

        sendMessage(chatId, "Укажите номер телефона для связи:");
    }

    private void handlePhone(Long chatId, String text, LeadBotSession session) {
        if (isInvalidPhone(text)) {
            sendMessage(
                    chatId,
                    """
                    Похоже, номер телефона указан некорректно.
                    
                    Пример формата:
                    +79161234567
                    
                    Введите номер телефона ещё раз:
                    """
            );
            return;
        }

        session.setPhone(text);
        session.setStep(BotStep.AWAITING_COURSE);
        sessions.put(chatId, session);

        sendCourseChoiceKeyboard(chatId);
    }

    private void handleCourse(Long chatId, String text, LeadBotSession session) {
        if (text == null || text.isBlank()) {
            sendMessage(chatId, "Выберите курс из списка:");
            return;
        }

        if ("Другой курс".equalsIgnoreCase(text)) {
            session.setCourseId(null);
            session.setCourseName("Другой курс");
        } else {
            Course course = botContentService.findCourseByTitle(text)
                    .orElse(null);

            if (course == null) {
                sendMessage(chatId, "Не нашёл такой курс. Пожалуйста, выберите курс кнопкой из списка.");
                sendCourseChoiceKeyboard(chatId);
                return;
            }

            session.setCourseId(course.getId());
            session.setCourseName(course.getTitle());
        }

        session.setStep(BotStep.IDLE);

        try {
            createLead(session);
            sessions.remove(chatId);

            sendMessageWithMainKeyboard(
                    chatId,
                    "✅ Заявка принята! Наш менеджер свяжется с вами в ближайшее время.\n\nСпасибо, "
                            + session.getFirstName()
                            + "!"
            );
        } catch (Exception e) {
            log.error("Failed to create lead from Telegram bot. chatId={}", chatId, e);

            sendMessageWithMainKeyboard(
                    chatId,
                    """
                    Не удалось создать заявку из-за временной ошибки.
                    
                    Пожалуйста, попробуйте позже или свяжитесь с нами напрямую.
                    """
            );
        }
    }

    private void createLead(LeadBotSession session) {
        LeadCreateRequest request = new LeadCreateRequest();

        request.setFirstName(session.getFirstName());
        request.setLastName(session.getLastName());
        request.setPhone(session.getPhone());
        request.setInterestedCourseId(session.getCourseId());
        request.setSource(SOURCE_TELEGRAM_BOT);

        leadService.create(request);
    }

    private void cancelDialog(Long chatId) {
        sessions.remove(chatId);

        sendMessageWithMainKeyboard(
                chatId,
                """
                Текущий диалог отменён.
                
                Вы можете выбрать действие в меню.
                """
        );
    }

    private void sendUnknownCommand(Long chatId) {
        sendMessageWithMainKeyboard(
                chatId,
                """
                Я не понял команду.
                
                Используйте меню ниже или команды:
                /tip — совет дня
                /courses — наши курсы
                /apply — оставить заявку
                /faq — FAQ
                /cancel — отменить диалог
                """
        );
    }

    private void sendCourseChoiceKeyboard(Long chatId) {
        List<String> courseNames = botContentService.getActiveCourseNames();

        List<KeyboardRow> rows = new ArrayList<>();

        KeyboardRow currentRow = new KeyboardRow();

        for (String courseName : courseNames) {
            currentRow.add(courseName);

            if (currentRow.size() == 2) {
                rows.add(currentRow);
                currentRow = new KeyboardRow();
            }
        }

        if (!currentRow.isEmpty()) {
            rows.add(currentRow);
        }

        KeyboardRow cancelRow = new KeyboardRow();
        cancelRow.add("/cancel");
        rows.add(cancelRow);

        ReplyKeyboardMarkup keyboardMarkup = ReplyKeyboardMarkup.builder()
                .resizeKeyboard(true)
                .oneTimeKeyboard(true)
                .keyboard(rows)
                .build();

        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text("Какой курс вас интересует?")
                .replyMarkup(keyboardMarkup)
                .build();

        execute(message);
    }

    private void sendMessage(Long chatId, String text) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .build();

        execute(message);
    }

    private void sendMessageWithMainKeyboard(Long chatId, String text) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .replyMarkup(createMainKeyboard())
                .build();

        execute(message);
    }

    private void sendMessageWithRemoveKeyboard(Long chatId, String text) {
        ReplyKeyboardRemove keyboardRemove = ReplyKeyboardRemove.builder()
                .removeKeyboard(true)
                .build();

        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .replyMarkup(keyboardRemove)
                .build();

        execute(message);
    }

    private ReplyKeyboardMarkup createMainKeyboard() {
        KeyboardRow firstRow = new KeyboardRow();
        firstRow.add(BUTTON_TIP);
        firstRow.add(BUTTON_COURSES);

        KeyboardRow secondRow = new KeyboardRow();
        secondRow.add(BUTTON_APPLY);
        secondRow.add(BUTTON_FAQ);

        return ReplyKeyboardMarkup.builder()
                .resizeKeyboard(true)
                .oneTimeKeyboard(false)
                .keyboard(List.of(firstRow, secondRow))
                .build();
    }

    private void execute(SendMessage message) {
        try {
            telegramClient.execute(message);
        } catch (Exception e) {
            log.error("Failed to send Telegram message. chatId={}", message.getChatId(), e);
        }
    }

    private String normalizeText(String text) {
        if (text == null) {
            return "";
        }

        return text.trim();
    }

    private boolean isStartCommand(String text) {
        return "/start".equalsIgnoreCase(text);
    }

    private boolean isMainMenuCommand(String text) {
        return BUTTON_MAIN_MENU.equalsIgnoreCase(text)
                || "меню".equalsIgnoreCase(text)
                || "главное меню".equalsIgnoreCase(text);
    }

    private boolean isCancelCommand(String text) {
        return "/cancel".equalsIgnoreCase(text)
                || "отмена".equalsIgnoreCase(text)
                || "cancel".equalsIgnoreCase(text);
    }

    private boolean isTipCommand(String text) {
        return "/tip".equalsIgnoreCase(text)
                || BUTTON_TIP.equalsIgnoreCase(text)
                || "совет дня".equalsIgnoreCase(text);
    }

    private boolean isCoursesCommand(String text) {
        return "/courses".equalsIgnoreCase(text)
                || BUTTON_COURSES.equalsIgnoreCase(text)
                || "курсы".equalsIgnoreCase(text)
                || "наши курсы".equalsIgnoreCase(text);
    }

    private boolean isFaqCommand(String text) {
        return "/faq".equalsIgnoreCase(text)
                || BUTTON_FAQ.equalsIgnoreCase(text)
                || "faq".equalsIgnoreCase(text);
    }

    private boolean isApplyCommand(String text) {
        return "/apply".equalsIgnoreCase(text)
                || BUTTON_APPLY.equalsIgnoreCase(text)
                || "оставить заявку".equalsIgnoreCase(text)
                || "заявка".equalsIgnoreCase(text);
    }

    private boolean isInvalidName(String text) {
        return text == null || text.isBlank() || text.trim().length() < 2 || text.length() > 100;
    }

    private boolean isInvalidPhone(String text) {
        if (text == null || text.isBlank()) {
            return true;
        }

        String normalized = text.replaceAll("[\\s()\\-]", "");

        return !normalized.matches("^\\+?[0-9]{10,15}$");
    }
}

package com.covenantcode.crm.telegram.manager.service.impl;

import com.covenantcode.crm.entity.Lead;
import com.covenantcode.crm.entity.Student;
import com.covenantcode.crm.entity.enums.LeadStatus;
import com.covenantcode.crm.telegram.manager.config.TelegramProperties;
import com.covenantcode.crm.telegram.manager.service.TelegramNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.format.DateTimeFormatter;

@Slf4j
@RequiredArgsConstructor
public class TelegramNotificationServiceImpl implements TelegramNotificationService {

    private final TelegramClient telegramClient;
    private final TelegramProperties telegramProperties;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @Override
    public void notifyLeadCreated(Lead lead) {
        String message = formatLeadCreatedMessage(lead);
        sendMessageToDefaultChat(message);
    }

    @Override
    public void notifyLeadStatusChanged(Lead lead, LeadStatus oldStatus) {
        String message = formatLeadStatusChangedMessage(lead, oldStatus);
        sendMessageToDefaultChat(message);
    }

    @Override
    public void notifyLeadConverted(Lead lead, Student student) {
        String message = formatLeadConvertedMessage(lead, student);
        sendMessageToDefaultChat(message);
    }

    @Override
    public void notifyStudentCreated(Student student) {
        String message = formatStudentCreatedMessage(student);
        sendMessageToDefaultChat(message);
    }

    private String formatLeadCreatedMessage(Lead lead) {
        StringBuilder sb = new StringBuilder();
        sb.append("🔔 *Новый лид*\n\n");
        appendFullName(sb, "👤 Имя: ", lead.getFirstName(), lead.getLastName());
        sb.append("📞 Телефон: ").append(lead.getPhone() != null ? lead.getPhone() : "не указан").append("\n");
        sb.append("📧 Email: ").append(lead.getEmail() != null ? lead.getEmail() : "не указан").append("\n");

        if (lead.getInterestedCourse() != null) {
            sb.append("📚 Курс: ").append(lead.getInterestedCourse().getTitle()).append("\n");
        }

        sb.append("📋 Статус: ").append(lead.getStatus().name()).append("\n");
        sb.append("🕐 Создан: ").append(lead.getCreatedAt().format(DATE_FORMATTER));

        return sb.toString();
    }

    private String formatLeadStatusChangedMessage(Lead lead, LeadStatus oldStatus) {
        StringBuilder sb = new StringBuilder();
        sb.append("🔄 *Статус лида изменён*\n\n");
        appendFullName(sb, "👤 Лид: ", lead.getFirstName(), lead.getLastName());
        sb.append("📋 Было: ").append(oldStatus.name()).append(" → Стало: ").append(lead.getStatus().name()).append("\n");
        sb.append("🕐 Изменено: ").append(lead.getUpdatedAt().format(DATE_FORMATTER));

        return sb.toString();
    }

    private String formatLeadConvertedMessage(Lead lead, Student student) {
        StringBuilder sb = new StringBuilder();
        sb.append("🎉 *Лид конвертирован в студента*\n\n");
        appendFullName(sb, "👤 Лид: ", lead.getFirstName(), lead.getLastName());
        sb.append("🎓 Студент ID: ").append(student.getId()).append("\n");
        sb.append("🕐 Конвертирован: ").append(lead.getUpdatedAt().format(DATE_FORMATTER));

        return sb.toString();
    }

    private String formatStudentCreatedMessage(Student student) {
        StringBuilder sb = new StringBuilder();
        sb.append("🎓 *Новый студент создан*\n\n");
        appendFullName(sb, "👤 Имя: ", student.getFirstName(), student.getLastName());

        if (student.getPhone() != null) {
            sb.append("📞 Телефон: ").append(student.getPhone()).append("\n");
        }
        if (student.getEmail() != null) {
            sb.append("📧 Email: ").append(student.getEmail()).append("\n");
        }

        sb.append("🕐 Создан: ").append(student.getCreatedAt().format(DATE_FORMATTER));
        sb.append("\n🎓 ID студента: ").append(student.getId());

        return sb.toString();
    }

    private void appendFullName(StringBuilder sb, String label, String firstName, String lastName) {
        sb.append(label).append(firstName).append(" ").append(lastName).append("\n");
    }

    private void sendMessageToDefaultChat(String message) {
        String chatId = telegramProperties.getDefaultChatId();

        if (chatId == null || chatId.isEmpty()) {
            log.warn("Cannot send telegram message: default chat id is not configured");
            return;
        }

        try {
            SendMessage sendMessage = SendMessage.builder()
                    .chatId(chatId)
                    .text(message)
                    .parseMode("Markdown")
                    .build();

            telegramClient.execute(sendMessage);
            log.debug("Telegram message sent successfully to chatId: {}", chatId);
        } catch (TelegramApiException e) {
            log.warn("Failed to send telegram message to chatId: {}", chatId, e);
        } catch (Exception e) {
            log.error("Unexpected error while sending telegram message", e);
        }
    }
}

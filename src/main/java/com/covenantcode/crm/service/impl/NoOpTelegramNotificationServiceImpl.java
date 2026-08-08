package com.covenantcode.crm.service.impl;

import com.covenantcode.crm.entity.Lead;
import com.covenantcode.crm.entity.Student;
import com.covenantcode.crm.entity.enums.LeadStatus;
import com.covenantcode.crm.service.TelegramNotificationService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NoOpTelegramNotificationServiceImpl implements TelegramNotificationService {
    @Override
    public void notifyLeadCreated(Lead lead) {
        log.info("Telegram disabled - would send notification for lead created: {}", lead.getId());
    }

    @Override
    public void notifyLeadStatusChanged(Lead lead, LeadStatus oldStatus) {
        log.info("Telegram disabled - would send notification for lead status changed: {} -> {}",
                oldStatus, lead.getStatus());

    }

    @Override
    public void notifyLeadConverted(Lead lead, Student student) {
        log.info("Telegram disabled - would send notification for lead converted: {} -> {}",
                lead.getId(), student.getId());
    }

    @Override
    public void notifyStudentCreated(Student student) {
        log.info("Telegram disabled - would send notification for student created: {}", student.getId());
    }
}

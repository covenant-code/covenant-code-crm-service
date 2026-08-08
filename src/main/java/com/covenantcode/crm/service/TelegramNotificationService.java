package com.covenantcode.crm.service;

import com.covenantcode.crm.entity.Lead;
import com.covenantcode.crm.entity.Student;
import com.covenantcode.crm.entity.enums.LeadStatus;

public interface TelegramNotificationService {
    void notifyLeadCreated(Lead lead);

    void notifyLeadStatusChanged(Lead lead, LeadStatus oldStatus);

    void notifyLeadConverted(Lead lead, Student student);

    void notifyStudentCreated(Student student);
}

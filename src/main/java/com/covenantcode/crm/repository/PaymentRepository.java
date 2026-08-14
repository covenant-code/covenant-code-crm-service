package com.covenantcode.crm.repository;

import com.covenantcode.crm.entity.Payment;
import com.covenantcode.crm.entity.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findAllByStudentId(Long studentId);
    List<Payment> findAllByStatus(PaymentStatus status);

    List<Payment> findAllByStatusAndDueDateBefore(PaymentStatus status, LocalDate date);
}

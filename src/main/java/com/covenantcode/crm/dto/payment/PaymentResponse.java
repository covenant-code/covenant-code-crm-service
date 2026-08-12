package com.covenantcode.crm.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {

    private Long id;
    private Long studentId;
    private String studentName;
    private BigDecimal amount;
    private String status;
    private String description;
    private LocalDate dueDate;
    private Long studyGroupId;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
}

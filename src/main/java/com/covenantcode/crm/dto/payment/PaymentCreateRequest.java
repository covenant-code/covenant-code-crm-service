package com.covenantcode.crm.dto.payment;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentCreateRequest {

    @NotNull(message = "Сумма платежа(руб) обязательно для заполнения")
    @Positive
    private BigDecimal amount;

    @Size(max = 500)
    private String description;

    private LocalDate dueDate;
    private Long studyGroupId;
}

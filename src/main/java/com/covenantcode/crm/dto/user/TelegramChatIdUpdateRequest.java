package com.covenantcode.crm.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос на обновление Telegram Chat ID")
public class TelegramChatIdUpdateRequest {
    @Schema(description = "Telegram Chat ID (числовой идентификатор)",
            example = "123456789", required = true)
    @NotBlank(message = "Telegram Chat ID не может быть пустым")
    @Pattern(regexp = "\\d+", message = "Telegram Chat ID должен содержать только цифры")
    private String telegramChatId;
}

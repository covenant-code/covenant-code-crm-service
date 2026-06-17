package com.covenantcode.crm.dto.lead;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Запрос на создание лида")
public class LeadCreateRequest {

    @Schema(description = "Имя лида", example = "Иван")
    @NotBlank(message = "Имя лида обязательно")
    @Size(max = 100, message = "Имя лида не должно превышать 100 символов")
    private String firstName;

    @Schema(description = "Фамилия лида", example = "Петров")
    @Size(max = 100, message = "Фамилия лида не должна превышать 100 символов")
    private String lastName;

    @Schema(description = "Телефон лида", example = "+79161234567")
    @NotBlank(message = "Номер телефона лида обязателен")
    @Size(max = 20, message = "Телефон лида не должен превышать 20 символов")
    private String phone;

    @Schema(description = "Email лида", example = "ivan.petrov@example.com")
    @Email(message = "Некорректный формат email")
    @Size(max = 255, message = "Email лида не должен превышать 255 символов")
    private String email;

    @Schema(description = "Источник обращения", example = "ВКонтакте")
    @Size(max = 255, message = "Источник обращения не должен превышать 255 символов")
    private String source;

    @Schema(description = "ID курса, которым интересуется лид", example = "1")
    private Long interestedCourseId;

    @Schema(description = "Произвольный комментарий при создании",
            example = "Перезвонить после обеда")
    private String comment;

    @Schema(description = "ID менеджера, которому назначается лид", example = "5")
    private Long assignedManagerId;
}

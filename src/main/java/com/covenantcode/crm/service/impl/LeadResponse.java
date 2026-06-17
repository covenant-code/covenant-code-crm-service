package com.covenantcode.crm.dto.lead;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Ответ с данными о лиде")
public class LeadResponse {

    @Schema(description = "Идентификатор лида", example = "1")
    private Long id;

    @Schema(description = "Имя лида", example = "Иван")
    private String firstName;

    @Schema(description = "Фамилия лида", example = "Петров")
    private String lastName;

    @Schema(description = "Телефон лида", example = "+79161234567")
    private String phone;

    @Schema(description = "Email лида", example = "ivan.petrov@gamil.com")
    private String email;

    @Schema(description = "Источник обращения", example = "реклама")
    private String source;

    @Schema(description = "Вложенный объект с id и title курса (или null)")
    private CourseShortResponse interestedCourse;

    @Schema(description = "Статус лида",
            example = "NEW",
            allowableValues = {"NEW", "IN_PROGRESS", "CONTACTED",
                    "CONVERTED_TO_STUDENT", "REJECTED"})
    private String status;

    @Schema(description = "Комментарий ", example = "бла бла бла")
    private String comment;

    @Schema(description = "Вложенный объект с id, firstName, lastName менеджера (или null)")
    private UserShortResponse assignedManager;

    @Schema(description = "ID студента при конвертации, null при создании")
    private Long convertedStudentId;

    @Schema(description = "Дата создания", example = "2025-01-15T10:00:00Z")
    private OffsetDateTime createdAt;

    @Schema(description = "Дата обновления", example = "2025-01-15T10:00:00Z")
    private OffsetDateTime updatedAt;
}

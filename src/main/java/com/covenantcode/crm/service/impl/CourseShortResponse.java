package com.covenantcode.crm.dto.lead;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Короткий ответ с данными о курсе")
public class CourseShortResponse {

    @Schema(description = "Идентификатор курса", example = "1")
    private Long id;

    @Schema(description = "Название курса", example = "Java Разработчик")
    private String title;
}

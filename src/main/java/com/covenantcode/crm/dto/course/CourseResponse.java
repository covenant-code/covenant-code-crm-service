package com.covenantcode.crm.dto.course;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description =  "Ответ с данными щ курсе")
public class CourseResponse {

    @Schema(description = "Идентификатор курса", example = "1")
    Long id;

    @Schema(description = "Название", example = "Java-developer pro max plus platinum")
    String title;

    @Schema(description = "Описание")
    String description;

    @Schema(description = "Продолжительность курса", example = "6")
    Integer durationInWeeks;

    @Schema(description = "Цена", example = "100000")
    BigDecimal price;

    @Schema(description = "Статус курса", example = "ACTIVE",
            allowableValues = {"ACTIVE", "ARCHIVED"})
    String status;

    @Schema(description = "Дата создания", example = "2025-01-15T10:00:00Z")
    LocalDateTime createdAt;

    @Schema(description = "Дата обновления", example = "2025-01-15T10:00:00Z")
    LocalDateTime updatedAt;
}

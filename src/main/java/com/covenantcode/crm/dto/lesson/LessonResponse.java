package com.covenantcode.crm.dto.lesson;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;

@Data
@Builder
public class LessonResponse {

    private Long id;
    private Long studyGroupId;
    private Long teacherId;
    private String topic;
    private String description;
    private LocalDate lessonDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private String teacherEmail;
}

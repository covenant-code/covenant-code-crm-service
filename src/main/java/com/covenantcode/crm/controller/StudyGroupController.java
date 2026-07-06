package com.covenantcode.crm.controller;

import com.covenantcode.crm.dto.group.StudyGroupCreateRequest;
import com.covenantcode.crm.dto.group.StudyGroupResponse;
import com.covenantcode.crm.service.StudyGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
@Tag(name = "Учебные группы", description = "API для управления учебными группами")
public class StudyGroupController {

    private final StudyGroupService studyGroupService;

    @Operation(summary = "Создание новой учебной группы", description = "Создаёт группу со статусом DRAFT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Группа успешно создана",
                    content = @Content(schema = @Schema(implementation = StudyGroupResponse.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации или бизнес-логики"),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(responseCode = "403", description = "Нет прав доступа"),
            @ApiResponse(responseCode = "404", description = "Ресурс не найден")
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<StudyGroupResponse> create(@Valid @RequestBody StudyGroupCreateRequest request) {
        StudyGroupResponse response = studyGroupService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

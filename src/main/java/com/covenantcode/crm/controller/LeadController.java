package com.covenantcode.crm.controller;

import com.covenantcode.crm.dto.lead.LeadCreateRequest;
import com.covenantcode.crm.dto.lead.LeadResponse;
import com.covenantcode.crm.service.LeadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/leads")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Управление лидами", description = "Эндпоинты для управления лидами")
public class LeadController {

    private final LeadService leadService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Создание нового лида")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Лид успешно создан"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации"),
            @ApiResponse(responseCode = "401", description = "Не авторизован - отсутствует или невалидный JWT токен"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещён - у пользователя нет роли ADMIN или MANAGER"),
            @ApiResponse(responseCode = "404", description = "Курс или менеджер не найден"),
    })
    public LeadResponse create(@Valid @RequestBody LeadCreateRequest request){
        return leadService.create(request);
    }
}

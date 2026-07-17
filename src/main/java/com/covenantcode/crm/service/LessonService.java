package com.covenantcode.crm.service;

import com.covenantcode.crm.dto.lesson.LessonCreateRequest;
import com.covenantcode.crm.dto.lesson.LessonResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface LessonService {

    Page<LessonResponse> getAll(Pageable pageable);

    LessonResponse getById(Long id);

    LessonResponse create(LessonCreateRequest request);

    LessonResponse update(Long id, LessonCreateRequest request);

    void delete(Long id);

}

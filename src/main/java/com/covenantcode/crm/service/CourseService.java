package com.covenantcode.crm.service;

import com.covenantcode.crm.dto.course.CourseCreateRequest;
import com.covenantcode.crm.dto.course.CourseResponse;

public interface CourseService {
    CourseResponse create(CourseCreateRequest request);

    void delete(Long id);
}
    CourseResponse getById(Long id);
}

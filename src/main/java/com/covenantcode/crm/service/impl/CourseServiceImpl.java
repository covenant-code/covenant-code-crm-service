package com.covenantcode.crm.service.impl;

import com.covenantcode.crm.dto.course.CourseCreateRequest;
import com.covenantcode.crm.dto.course.CourseResponse;
import com.covenantcode.crm.entity.Course;
import com.covenantcode.crm.entity.enums.CourseStatus;
import com.covenantcode.crm.mapper.CourseMapper;
import com.covenantcode.crm.repository.CourseRepository;
import com.covenantcode.crm.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {
    private final CourseMapper courseMapper;
    private final CourseRepository courseRepository;

    @Override
    @Transactional
    public CourseResponse create(CourseCreateRequest request) {
        if(request.getStatus() == null || request.getStatus().isEmpty()){
            request.setStatus(CourseStatus.ACTIVE.name());
        }

        Course course = courseMapper.toEntity(request);

        Course savedCourse = courseRepository.save(course);
        return courseMapper.toResponse(savedCourse);
    }
}

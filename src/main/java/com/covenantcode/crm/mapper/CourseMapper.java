package com.covenantcode.crm.mapper;

import com.covenantcode.crm.dto.course.CourseCreateRequest;
import com.covenantcode.crm.dto.course.CourseResponse;
import com.covenantcode.crm.entity.Course;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface CourseMapper {

    @Mapping(target = "createdAt", expression = "java(course.getCreatedAt().toLocalDateTime())")
    @Mapping(target = "updatedAt", expression = "java(course.getUpdatedAt().toLocalDateTime())")
    CourseResponse toResponse(Course course);

    Course toEntity(CourseCreateRequest request);
}

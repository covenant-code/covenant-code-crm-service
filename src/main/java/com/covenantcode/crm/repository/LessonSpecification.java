package com.covenantcode.crm.repository;

import com.covenantcode.crm.entity.Lesson;
import org.springframework.data.jpa.domain.Specification;

public class LessonSpecification {

    public static Specification<Lesson> hasTeacherId(Long teacherId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("teacher").get("id"), teacherId);
    }
}

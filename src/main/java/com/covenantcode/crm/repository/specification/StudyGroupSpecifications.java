package com.covenantcode.crm.repository.specification;

import com.covenantcode.crm.entity.StudyGroup;
import com.covenantcode.crm.entity.enums.GroupStatus;
import java.util.Objects;
import org.springframework.data.jpa.domain.Specification;

public final class StudyGroupSpecifications {

    private StudyGroupSpecifications() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static Specification<StudyGroup> byCourseId(Long courseId) {
        return (root, query, cb) -> {
            if (Objects.isNull(courseId)) {
                return cb.conjunction();
            }
            return cb.equal(root.join("course").get("id"), courseId);
        };
    }

    public static Specification<StudyGroup> byTeacherId(Long teacherId) {
        return (root, query, cb) -> {
            if (Objects.isNull(teacherId)) {
                return cb.conjunction();
            }
            return cb.equal(root.join("teacher").get("id"), teacherId);
        };
    }

    public static Specification<StudyGroup> byStatus(GroupStatus status) {
        return (root, query, cb) -> {
            if (Objects.isNull(status)) {
                return cb.conjunction();
            }
            return cb.equal(root.get("status"), status);
        };
    }
}


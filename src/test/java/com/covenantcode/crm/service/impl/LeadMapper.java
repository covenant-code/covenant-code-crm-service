package com.covenantcode.crm.mapper;

import com.covenantcode.crm.dto.lead.LeadCreateRequest;
import com.covenantcode.crm.dto.lead.LeadResponse;
import com.covenantcode.crm.entity.Lead;
import com.covenantcode.crm.entity.Student;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

@Mapper(uses = {CourseMapper.class, UserMapper.class})
public interface LeadMapper {
    @Mapping(target = "interestedCourse", source = "interestedCourse")
    @Mapping(target = "assignedManager", source = "assignedManager")
    @Mapping(target = "convertedStudentId", source = "convertedStudent", qualifiedByName = "studentToId")
    LeadResponse toResponse(Lead lead);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "interestedCourse", ignore = true)
    @Mapping(target = "assignedManager", ignore = true)
    @Mapping(target = "convertedStudent", ignore = true)
    @Mapping(target = "status", constant = "NEW")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Lead toEntity(LeadCreateRequest request);

    @Named("studentToId")
    default Long studentToId(Student student) {
        return student != null ? student.getId() : null;
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "interestedCourse", ignore = true)
    @Mapping(target = "assignedManager", ignore = true)
    @Mapping(target = "convertedStudent", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(LeadCreateRequest request, @MappingTarget Lead lead);
}

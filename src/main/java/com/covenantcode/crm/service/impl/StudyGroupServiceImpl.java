package com.covenantcode.crm.service.impl;

import com.covenantcode.crm.dto.group.StudyGroupCreateRequest;
import com.covenantcode.crm.dto.group.StudyGroupResponse;
import com.covenantcode.crm.entity.Course;
import com.covenantcode.crm.entity.Student;
import com.covenantcode.crm.entity.StudyGroup;
import com.covenantcode.crm.entity.User;
import com.covenantcode.crm.entity.enums.GroupStatus;
import com.covenantcode.crm.entity.enums.RoleName;
import com.covenantcode.crm.exception.BadRequestException;
import com.covenantcode.crm.exception.ResourceNotFoundException;
import com.covenantcode.crm.mapper.StudyGroupMapper;
import com.covenantcode.crm.repository.CourseRepository;
import com.covenantcode.crm.repository.StudentRepository;
import com.covenantcode.crm.repository.StudyGroupRepository;
import com.covenantcode.crm.repository.UserRepository;
import com.covenantcode.crm.repository.specification.StudyGroupSpecifications;
import com.covenantcode.crm.service.StudyGroupService;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class StudyGroupServiceImpl implements StudyGroupService {

    private final StudyGroupRepository studyGroupRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final StudyGroupMapper studyGroupMapper;

    @Override
    @Transactional
    public StudyGroupResponse create(StudyGroupCreateRequest request) {
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course", request.getCourseId()));

        User teacher = userRepository.findById(request.getTeacherId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getTeacherId()));

        if (teacher.getRole() == null || !RoleName.TEACHER.equals(teacher.getRole().getName())) {
            throw new BadRequestException("Пользователь с id " + request.getTeacherId() + " не является учителем");
        }

        Set<Student> students = new HashSet<>();
        if (request.getStudentIds() != null && !request.getStudentIds().isEmpty()) {
            for (Long studentId : request.getStudentIds()) {
                Student student = studentRepository.findById(studentId)
                        .orElseThrow(() -> new ResourceNotFoundException("Student", studentId));
                students.add(student);
            }
        }

        StudyGroup group = StudyGroup.builder()
                .name(request.getName())
                .course(course)
                .teacher(teacher)
                .startDate(request.getStartDate())
                .students(students)
                .status(GroupStatus.DRAFT)
                .build();

        StudyGroup savedGroup = studyGroupRepository.save(group);

        return studyGroupMapper.toResponse(savedGroup);
    }
    @Override
    @Transactional(readOnly = true)
    public Page<StudyGroupResponse> getAll(Long courseId, Long teacherId, GroupStatus status, Pageable pageable) {
        Specification<StudyGroup> spec = buildFilter(courseId, teacherId, status);


        return studyGroupRepository.findAll(spec, pageable)
                .map(studyGroupMapper::toResponse);
    }
    private Specification<StudyGroup> buildFilter(Long courseId, Long teacherId, GroupStatus status) {
        return Specification.<StudyGroup>where(null)
                .and(StudyGroupSpecifications.byCourseId(courseId))
                .and(StudyGroupSpecifications.byTeacherId(teacherId))
                .and(StudyGroupSpecifications.byStatus(status));
    }
}
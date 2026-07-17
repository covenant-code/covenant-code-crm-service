package com.covenantcode.crm.service.impl;

import com.covenantcode.crm.dto.group.GroupStatusUpdateRequest;
import com.covenantcode.crm.dto.group.StudyGroupCreateRequest;
import com.covenantcode.crm.dto.group.StudyGroupResponse;
import com.covenantcode.crm.dto.group.StudyGroupUpdateRequest;
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
import java.util.Map;
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

    private static final Map<GroupStatus, Set<GroupStatus>> ALLOWED_TRANSITIONS = Map.of(
            GroupStatus.DRAFT, Set.of(GroupStatus.ACTIVE),
            GroupStatus.ACTIVE, Set.of(GroupStatus.COMPLETED, GroupStatus.CANCELLED),
            GroupStatus.COMPLETED, Set.of(),
            GroupStatus.CANCELLED, Set.of()
    );

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
    @Override
    @Transactional
    public StudyGroupResponse update(Long id, StudyGroupUpdateRequest request) {
        // 1. Поиск существующей группы
        StudyGroup existingGroup = studyGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StudyGroup с id " + id + " не найдена"));

        // 2. Проверка статуса группы (финальные статусы)
        if (existingGroup.getStatus() == GroupStatus.COMPLETED || existingGroup.getStatus() == GroupStatus.CANCELLED) {
            throw new BadRequestException("Нельзя редактировать группу в статусе " + existingGroup.getStatus());
        }

        // 3. Загрузка и проверка нового курса
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course с id " + request.getCourseId() + " не найден"));

        // 4. Загрузка и проверка нового учителя
        User teacher = userRepository.findById(request.getTeacherId())
                .orElseThrow(() -> new ResourceNotFoundException("User с id " + request.getTeacherId() + " не найден"));

        // 5. Проверка роли учителя
        if (teacher.getRole() == null || !RoleName.TEACHER.equals(teacher.getRole().getName())) {
            throw new BadRequestException("Пользователь с id " + request.getTeacherId() + " не является учителем");
        }

        // 6. Обновление полей группы
        existingGroup.setName(request.getName());
        existingGroup.setCourse(course);
        existingGroup.setTeacher(teacher);
        existingGroup.setStartDate(request.getStartDate());

        // 7. Сохранение и возврат результата через маппер
        StudyGroup updatedGroup = studyGroupRepository.save(existingGroup);
        return studyGroupMapper.toResponse(updatedGroup);
    }

    @Override
    @Transactional
    public StudyGroupResponse updateStatus(Long id, GroupStatusUpdateRequest request) {

        StudyGroup group = studyGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StudyGroup с id " + id + " не найдена"));

        GroupStatus currentStatus = group.getStatus();
        GroupStatus newStatus = request.getStatus();

        if (!isTransitionAllowed(currentStatus, newStatus)) {
            throw new BadRequestException(
                    String.format("Переход из %s в %s недопустим", currentStatus, newStatus)
            );
        }

        group.setStatus(newStatus);

        StudyGroup savedGroup = studyGroupRepository.save(group);
        return studyGroupMapper.toResponse(savedGroup);
    }

    private boolean isTransitionAllowed(GroupStatus currentStatus, GroupStatus newStatus) {
        Set<GroupStatus> allowedStatuses = ALLOWED_TRANSITIONS.get(currentStatus);
        return allowedStatuses != null && allowedStatuses.contains(newStatus);
    }
}
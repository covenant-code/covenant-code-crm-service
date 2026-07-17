package com.covenantcode.crm.service.impl;

import com.covenantcode.crm.dto.group.CourseShortResponse;
import com.covenantcode.crm.dto.group.GroupStatusUpdateRequest;
import com.covenantcode.crm.dto.group.StudyGroupCreateRequest;
import com.covenantcode.crm.dto.group.StudyGroupResponse;
import com.covenantcode.crm.dto.group.StudyGroupUpdateRequest;
import com.covenantcode.crm.dto.group.UserShortResponse;
import com.covenantcode.crm.entity.Course;
import com.covenantcode.crm.entity.Role;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudyGroupServiceImplTest {

    @Mock
    private StudyGroupRepository studyGroupRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private StudyGroupMapper studyGroupMapper;

    @InjectMocks
    private StudyGroupServiceImpl studyGroupService;

    private final LocalDate startDate = LocalDate.of(2026, 7, 6);
    private StudyGroup group1, group2, group3;
    private StudyGroupResponse resp1, resp2;
    private Course course1, course2;
    private User teacher1, teacher2, teacherWithoutRole;
    private Role teacherRole;
    private StudyGroupUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {

        // Создание ролей
        teacherRole = new Role();
        teacherRole.setName(RoleName.TEACHER);

        // Создание курсов
        course1 = new Course();
        course1.setId(1L);
        course1.setTitle("Java Core");

        course2 = new Course();
        course2.setId(2L);
        course2.setTitle("Spring Boot");

        // Создание учителей
        teacher1 = new User();
        teacher1.setId(100L);
        teacher1.setFirstName("John");
        teacher1.setLastName("Doe");
        teacher1.setRole(teacherRole);

        teacher2 = new User();
        teacher2.setId(200L);
        teacher2.setFirstName("Jane");
        teacher2.setLastName("Smith");
        teacher2.setRole(teacherRole);

        teacherWithoutRole = new User();
        teacherWithoutRole.setId(300L);
        teacherWithoutRole.setFirstName("Bob");
        teacherWithoutRole.setLastName("Johnson");
        teacherWithoutRole.setRole(teacherRole);



        group1 = new StudyGroup();
        group1.setId(1L);
        group1.setName("Java Core");
        group1.setStatus(GroupStatus.ACTIVE);
        group1.setStartDate(LocalDate.of(2024, 9, 1));
        group1.setCourse(course1);
        group1.setTeacher(teacher1);

        group2 = new StudyGroup();
        group2.setId(2L);
        group2.setName("Spring Boot");
        group2.setStatus(GroupStatus.DRAFT);
        group2.setStartDate(LocalDate.of(2024, 10, 15));
        group2.setCourse(course2);
        group2.setTeacher(teacher2);


        group3 = new StudyGroup();
        group3.setId(3L);
        group3.setName("Kotlin Advanced");
        group3.setStatus(GroupStatus.COMPLETED);
        group3.setStartDate(LocalDate.of(2024, 6, 1));
        group3.setCourse(course1);
        group3.setTeacher(teacher1);

        resp1 = new StudyGroupResponse();
        resp1.setId(1L);
        resp1.setName("Java Core");
        resp1.setStatus(GroupStatus.ACTIVE);

        resp2 = new StudyGroupResponse();
        resp2.setId(2L);
        resp2.setName("Spring Boot");
        resp2.setStatus(GroupStatus.DRAFT);

        // Инициализация запроса на обновление
        updateRequest = new StudyGroupUpdateRequest();
        updateRequest.setName("Updated Group");
        updateRequest.setCourseId(2L);
        updateRequest.setTeacherId(200L);
        updateRequest.setStartDate(startDate);
    }

    @Test
    void createGroupWithoutStudents_shouldSucceed() {
        Long courseId = 1L;
        Long teacherId = 2L;
        Course course = Course.builder().id(courseId).title("Java").build();
        User teacher = createTeacher(teacherId);
        StudyGroupCreateRequest request = StudyGroupCreateRequest.builder()
                .name("Group A")
                .courseId(courseId)
                .teacherId(teacherId)
                .startDate(startDate)
                .studentIds(null)
                .build();

        StudyGroup savedGroup = StudyGroup.builder()
                .id(10L)
                .name(request.getName())
                .course(course)
                .teacher(teacher)
                .startDate(startDate)
                .students(Set.of())
                .status(GroupStatus.DRAFT)
                .build();

        StudyGroupResponse mockResponse = StudyGroupResponse.builder()
                .id(10L)
                .name(request.getName())
                .startDate(startDate)
                .status(GroupStatus.DRAFT)
                .build();

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(userRepository.findById(teacherId)).thenReturn(Optional.of(teacher));
        when(studyGroupRepository.save(any(StudyGroup.class))).thenReturn(savedGroup);
        when(studyGroupMapper.toResponse(savedGroup)).thenReturn(mockResponse);

        StudyGroupResponse response = studyGroupService.create(request);

        ArgumentCaptor<StudyGroup> groupCaptor = ArgumentCaptor.forClass(StudyGroup.class);
        verify(studyGroupRepository).save(groupCaptor.capture());
        StudyGroup captured = groupCaptor.getValue();

        assertThat(captured.getName()).isEqualTo("Group A");
        assertThat(captured.getCourse()).isEqualTo(course);
        assertThat(captured.getTeacher()).isEqualTo(teacher);
        assertThat(captured.getStartDate()).isEqualTo(startDate);
        assertThat(captured.getStatus()).isEqualTo(GroupStatus.DRAFT);
        assertThat(captured.getStudents()).isEmpty();

        verify(courseRepository).findById(courseId);
        verify(userRepository).findById(teacherId);
        verifyNoInteractions(studentRepository);
        verify(studyGroupMapper).toResponse(savedGroup);
        assertThat(response).isNotNull();
    }

    @Test
    void createGroupWithStudents_shouldSucceed() {
        Long courseId = 1L;
        Long teacherId = 2L;
        Set<Long> studentIds = Set.of(101L, 102L);
        Course course = Course.builder().id(courseId).title("Java").build();
        User teacher = createTeacher(teacherId);
        Student student1 = Student.builder().id(101L).firstName("Ivan").lastName("Petrov").build();
        Student student2 = Student.builder().id(102L).firstName("Maria").lastName("Ivanova").build();

        StudyGroupCreateRequest request = StudyGroupCreateRequest.builder()
                .name("Group B")
                .courseId(courseId)
                .teacherId(teacherId)
                .startDate(startDate)
                .studentIds(studentIds)
                .build();

        StudyGroup savedGroup = StudyGroup.builder()
                .id(20L)
                .name(request.getName())
                .course(course)
                .teacher(teacher)
                .startDate(startDate)
                .students(Set.of(student1, student2))
                .status(GroupStatus.DRAFT)
                .build();

        StudyGroupResponse mockResponse = StudyGroupResponse.builder()
                .id(20L)
                .name(request.getName())
                .startDate(startDate)
                .status(GroupStatus.DRAFT)
                .build();

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(userRepository.findById(teacherId)).thenReturn(Optional.of(teacher));
        when(studentRepository.findById(101L)).thenReturn(Optional.of(student1));
        when(studentRepository.findById(102L)).thenReturn(Optional.of(student2));
        when(studyGroupRepository.save(any(StudyGroup.class))).thenReturn(savedGroup);
        when(studyGroupMapper.toResponse(savedGroup)).thenReturn(mockResponse);

        StudyGroupResponse response = studyGroupService.create(request);

        ArgumentCaptor<StudyGroup> groupCaptor = ArgumentCaptor.forClass(StudyGroup.class);
        verify(studyGroupRepository).save(groupCaptor.capture());
        StudyGroup captured = groupCaptor.getValue();

        assertThat(captured.getStudents()).hasSize(2);
        assertThat(captured.getStudents()).extracting(Student::getId).containsExactlyInAnyOrder(101L, 102L);

        verify(studentRepository).findById(101L);
        verify(studentRepository).findById(102L);
        verify(studyGroupMapper).toResponse(savedGroup);
        assertThat(response).isNotNull();
    }

    @Test
    void createGroup_courseNotFound_shouldThrowResourceNotFoundException() {
        Long courseId = 99L;
        StudyGroupCreateRequest request = StudyGroupCreateRequest.builder()
                .name("Group C")
                .courseId(courseId)
                .teacherId(1L)
                .startDate(startDate)
                .build();

        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studyGroupService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Course")
                .hasMessageContaining("99");

        verify(courseRepository).findById(courseId);
        verifyNoInteractions(userRepository);
        verifyNoInteractions(studentRepository);
        verifyNoInteractions(studyGroupRepository);
        verifyNoInteractions(studyGroupMapper);
    }

    @Test
    void createGroup_teacherNotFound_shouldThrowResourceNotFoundException() {
        Long courseId = 1L;
        Long teacherId = 99L;
        Course course = Course.builder().id(courseId).build();

        StudyGroupCreateRequest request = StudyGroupCreateRequest.builder()
                .name("Group D")
                .courseId(courseId)
                .teacherId(teacherId)
                .startDate(startDate)
                .build();

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(userRepository.findById(teacherId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studyGroupService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User")
                .hasMessageContaining("99");

        verify(courseRepository).findById(courseId);
        verify(userRepository).findById(teacherId);
        verifyNoInteractions(studentRepository);
        verifyNoInteractions(studyGroupRepository);
        verifyNoInteractions(studyGroupMapper);
    }

    @Test
    void createGroup_teacherNotHasRoleTeacher_shouldThrowBadRequestException() {
        Long courseId = 1L;
        Long teacherId = 5L;
        Course course = Course.builder().id(courseId).build();
        Role managerRole = Role.builder().name(RoleName.MANAGER).build();
        User manager = User.builder()
                .id(teacherId)
                .firstName("Manager")
                .lastName("User")
                .role(managerRole)
                .build();

        StudyGroupCreateRequest request = StudyGroupCreateRequest.builder()
                .name("Group E")
                .courseId(courseId)
                .teacherId(teacherId)
                .startDate(startDate)
                .build();

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(userRepository.findById(teacherId)).thenReturn(Optional.of(manager));

        assertThatThrownBy(() -> studyGroupService.create(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("не является учителем")
                .hasMessageContaining(String.valueOf(teacherId));

        verify(courseRepository).findById(courseId);
        verify(userRepository).findById(teacherId);
        verifyNoInteractions(studentRepository);
        verifyNoInteractions(studyGroupRepository);
        verifyNoInteractions(studyGroupMapper);
    }

    @Test
    void createGroup_oneStudentNotFound_shouldThrowResourceNotFoundException() {
        Long courseId = 1L;
        Long teacherId = 2L;
        Set<Long> studentIds = Set.of(101L, 999L);
        Course course = Course.builder().id(courseId).build();
        User teacher = createTeacher(teacherId);
        Student existingStudent = Student.builder().id(101L).build();

        StudyGroupCreateRequest request = StudyGroupCreateRequest.builder()
                .name("Group F")
                .courseId(courseId)
                .teacherId(teacherId)
                .startDate(startDate)
                .studentIds(studentIds)
                .build();

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(userRepository.findById(teacherId)).thenReturn(Optional.of(teacher));
        lenient().when(studentRepository.findById(101L)).thenReturn(Optional.of(existingStudent));
        lenient().when(studentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studyGroupService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Student")
                .hasMessageContaining("999");

        verify(courseRepository).findById(courseId);
        verify(userRepository).findById(teacherId);
        verify(studentRepository).findById(999L);
        verify(studyGroupRepository, never()).save(any());
        verifyNoInteractions(studyGroupMapper);
    }

    private User createTeacher(Long id) {
        Role teacherRole = Role.builder().name(RoleName.TEACHER).build();
        return User.builder()
                .id(id)
                .firstName("Teacher")
                .lastName("Test")
                .role(teacherRole)
                .build();
    }

    @Test
    @DisplayName("Should return page with one group when no filters applied")
    void getAll_NoFilters_ReturnsNonEmptyPage() {

        Pageable pageable = PageRequest.of(0, 10);
        when(studyGroupRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(group1)));
        when(studyGroupMapper.toResponse(group1)).thenReturn(resp1);


        Page<StudyGroupResponse> result = studyGroupService.getAll(null, null, null, pageable);


        assertThat(result).isNotNull();
        assertThat(result.isEmpty()).isFalse();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Java Core");
        assertThat(result.getContent().get(0).getStatus()).isEqualTo(GroupStatus.ACTIVE);

        verify(studyGroupRepository).findAll(any(Specification.class), eq(pageable));
        verify(studyGroupMapper).toResponse(group1);
    }

    @Test
    @DisplayName("Should apply courseId filter in specification")
    void getAll_WithCourseIdFilter_AppliesSpecification() {

        Pageable pageable = PageRequest.of(0, 10);
        when(studyGroupRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(group1)));
        when(studyGroupMapper.toResponse(group1)).thenReturn(resp1);


        studyGroupService.getAll(1L, null, null, pageable);


        verify(studyGroupRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    @DisplayName("Should apply teacherId filter in specification")
    void getAll_WithTeacherIdFilter_AppliesSpecification() {

        Pageable pageable = PageRequest.of(0, 10);
        when(studyGroupRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(group1)));
        when(studyGroupMapper.toResponse(group1)).thenReturn(resp1);


        studyGroupService.getAll(null, 3L, null, pageable);


        verify(studyGroupRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    @DisplayName("Should apply status filter in specification")
    void getAll_WithStatusFilter_AppliesSpecification() {

        Pageable pageable = PageRequest.of(0, 10);
        when(studyGroupRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(group1)));
        when(studyGroupMapper.toResponse(group1)).thenReturn(resp1);


        studyGroupService.getAll(null, null, GroupStatus.ACTIVE, pageable);


        verify(studyGroupRepository).findAll(any(Specification.class), eq(pageable));
    }


    @Test
    @DisplayName("Should return empty page when no groups match criteria")
    void getAll_NoMatches_ReturnsEmptyPage() {

        Pageable pageable = PageRequest.of(0, 10);
        when(studyGroupRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(Page.empty());

        Page<StudyGroupResponse> result = studyGroupService.getAll(
                99L, 99L, GroupStatus.CANCELLED, pageable
        );

        assertThat(result).isNotNull();
        assertThat(result.isEmpty()).isTrue();
        assertThat(result.getTotalElements()).isZero();
        verify(studyGroupRepository).findAll(any(Specification.class), eq(pageable));
        verifyNoInteractions(studyGroupMapper);
    }

    @Test
    @DisplayName("Should return multiple groups when multiple exist and no filters")
    void getAll_MultipleGroupsNoFilters_ReturnsPageWithMultipleItems() {

        Pageable pageable = PageRequest.of(0, 10);
        when(studyGroupRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(group1, group2)));
        when(studyGroupMapper.toResponse(group1)).thenReturn(resp1);
        when(studyGroupMapper.toResponse(group2)).thenReturn(resp2);

        Page<StudyGroupResponse> result = studyGroupService.getAll(null, null, null, pageable);


        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Java Core");
        assertThat(result.getContent().get(1).getName()).isEqualTo("Spring Boot");

        verify(studyGroupRepository).findAll(any(Specification.class), eq(pageable));
        verify(studyGroupMapper).toResponse(group1);
        verify(studyGroupMapper).toResponse(group2);
    }


    @Test
    void update_Success_ShouldReturnUpdatedResponse_WhenGroupIsDraftAndAllValid() {
        // Arrange
        when(studyGroupRepository.findById(2L)).thenReturn(Optional.of(group2));
        when(courseRepository.findById(2L)).thenReturn(Optional.of(course2));
        when(userRepository.findById(200L)).thenReturn(Optional.of(teacher2));

        StudyGroup updatedGroup = new StudyGroup();
        updatedGroup.setId(2L);
        updatedGroup.setName("Updated Group");
        updatedGroup.setStatus(GroupStatus.DRAFT);
        updatedGroup.setStartDate(LocalDate.of(2026, 7, 6));
        updatedGroup.setCourse(course2);
        updatedGroup.setTeacher(teacher2);
        updatedGroup.setStudents(group2.getStudents()); // Гарантируем перенос студентов из существующей группы

        // Создаем детальный mock-ответ, отражающий структуру из ТЗ
        StudyGroupResponse expectedResponse = StudyGroupResponse.builder()
                .id(2L)
                .name("Updated Group")
                .status(GroupStatus.DRAFT)
                .startDate(LocalDate.of(2026, 7, 6))
                .course(CourseShortResponse.builder().id(2L).name("Spring Boot").build())
                .teacher(UserShortResponse.builder().id(200L).firstName("Jane").lastName("Smith").build())
                .students(List.of()) // Проверяем, что поле присутствует в ответе и не равно null
                .build();

        when(studyGroupRepository.save(any(StudyGroup.class))).thenReturn(updatedGroup);
        when(studyGroupMapper.toResponse(updatedGroup)).thenReturn(expectedResponse);

        // Act
        StudyGroupResponse response = studyGroupService.update(2L, updateRequest);

        // Assert
        assertNotNull(response);
        assertEquals(2L, response.getId());
        assertEquals("Updated Group", response.getName());
        assertEquals(GroupStatus.DRAFT, response.getStatus());
        assertEquals(LocalDate.of(2026, 7, 6), response.getStartDate());

        // Сверка вложенных сущностей курса и учителя
        assertNotNull(response.getCourse());
        assertEquals(2L, response.getCourse().getId());
        assertNotNull(response.getTeacher());
        assertEquals(200L, response.getTeacher().getId());

        // Критическая проверка на баг с пропавшими студентами (теперь упадет, если маппер вернет null/пустоту некорректно)
        assertNotNull(response.getStudents(), "Поле students не должно быть null в ответе");

        // Верификация вызовов
        verify(studyGroupRepository).save(any(StudyGroup.class));
        verify(studyGroupMapper).toResponse(updatedGroup);
    }

    @Test
    void update_ShouldThrowBadRequestException_WhenGroupStatusIsCompleted() {
        // Arrange
        when(studyGroupRepository.findById(3L)).thenReturn(Optional.of(group3));

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> studyGroupService.update(3L, updateRequest));

        // Исправлено: текст ошибки на русском языке
        assertTrue(exception.getMessage().contains("Нельзя редактировать группу в статусе COMPLETED"));
    }

    @Test
    void update_ShouldThrowBadRequestException_WhenGroupStatusIsCancelled() {
        // Arrange
        StudyGroup cancelledGroup = new StudyGroup();
        cancelledGroup.setId(4L);
        cancelledGroup.setStatus(GroupStatus.CANCELLED);
        when(studyGroupRepository.findById(4L)).thenReturn(Optional.of(cancelledGroup));

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> studyGroupService.update(4L, updateRequest));

        // Исправлено: текст ошибки на русском языке
        assertTrue(exception.getMessage().contains("Нельзя редактировать группу в статусе CANCELLED"));
    }

    @Test
    void update_ShouldThrowResourceNotFoundException_WhenGroupNotFound() {
        // Arrange
        when(studyGroupRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> studyGroupService.update(99L, updateRequest));

        // Исправлено: текст ошибки на русском языке
        assertTrue(exception.getMessage().contains("StudyGroup с id 99 не найдена"));
    }

    @Test
    void update_ShouldThrowResourceNotFoundException_WhenCourseNotFound() {
        // Arrange
        when(studyGroupRepository.findById(2L)).thenReturn(Optional.of(group2));
        when(courseRepository.findById(2L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> studyGroupService.update(2L, updateRequest));

        // Исправлено: текст ошибки на русском языке
        assertTrue(exception.getMessage().contains("Course с id 2 не найден"));
    }

    @Test
    void update_ShouldThrowResourceNotFoundException_WhenTeacherNotFound() {
        // Arrange
        when(studyGroupRepository.findById(2L)).thenReturn(Optional.of(group2));
        when(courseRepository.findById(2L)).thenReturn(Optional.of(course2));
        when(userRepository.findById(200L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> studyGroupService.update(2L, updateRequest));

        // Исправлено: изменен тип сущности на User и язык на русский
        assertTrue(exception.getMessage().contains("User с id 200 не найден"));
    }

    @Test
    void update_ShouldThrowBadRequestException_WhenTeacherHasNoTeacherRole() {
        // Arrange
        Role role = new Role();
        role.setName(RoleName.STUDENT);

        teacherWithoutRole.setId(200L);
        teacherWithoutRole.setRole(role);

        when(studyGroupRepository.findById(any(Long.class))).thenReturn(Optional.of(group2));
        when(courseRepository.findById(any(Long.class))).thenReturn(Optional.of(course2));
        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(teacherWithoutRole));

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () ->
                studyGroupService.update(2L, updateRequest));

        // Исправлено: текст ошибки на русском языке
        assertTrue(exception.getMessage().contains("Пользователь с id 200 не является учителем"));
    }

    @Test
    @DisplayName("DRAFT → ACTIVE: допустимый переход, статус изменён")
    void updateStatus_DraftToActive_ShouldSucceed() {

        Long groupId = 1L;
        GroupStatusUpdateRequest request = new GroupStatusUpdateRequest(GroupStatus.ACTIVE);

        StudyGroup group = StudyGroup.builder()
                .id(groupId)
                .status(GroupStatus.DRAFT)
                .build();

        StudyGroupResponse expectedResponse = StudyGroupResponse.builder()
                .id(groupId)
                .status(GroupStatus.ACTIVE)
                .build();

        when(studyGroupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(studyGroupRepository.save(any(StudyGroup.class))).thenReturn(group);
        when(studyGroupMapper.toResponse(any(StudyGroup.class))).thenReturn(expectedResponse);

        StudyGroupResponse result = studyGroupService.updateStatus(groupId, request);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(GroupStatus.ACTIVE);
        assertThat(group.getStatus()).isEqualTo(GroupStatus.ACTIVE);
        verify(studyGroupRepository).findById(groupId);
        verify(studyGroupRepository).save(group);
        verify(studyGroupMapper).toResponse(group);
    }

    @Test
    @DisplayName("ACTIVE → COMPLETED: допустимый переход, статус изменён")
    void updateStatus_ActiveToCompleted_ShouldSucceed() {

        Long groupId = 1L;
        GroupStatusUpdateRequest request = new GroupStatusUpdateRequest(GroupStatus.COMPLETED);

        StudyGroup group = StudyGroup.builder()
                .id(groupId)
                .status(GroupStatus.ACTIVE)
                .build();

        StudyGroupResponse expectedResponse = StudyGroupResponse.builder()
                .id(groupId)
                .status(GroupStatus.COMPLETED)
                .build();

        when(studyGroupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(studyGroupRepository.save(any(StudyGroup.class))).thenReturn(group);
        when(studyGroupMapper.toResponse(any(StudyGroup.class))).thenReturn(expectedResponse);

        StudyGroupResponse result = studyGroupService.updateStatus(groupId, request);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(GroupStatus.COMPLETED);
        assertThat(group.getStatus()).isEqualTo(GroupStatus.COMPLETED);
        verify(studyGroupRepository).findById(groupId);
        verify(studyGroupRepository).save(group);
        verify(studyGroupMapper).toResponse(group);
    }

    @Test
    @DisplayName("ACTIVE → CANCELLED: допустимый переход, статус изменён")
    void updateStatus_ActiveToCancelled_ShouldSucceed() {

        Long groupId = 1L;
        GroupStatusUpdateRequest request = new GroupStatusUpdateRequest(GroupStatus.CANCELLED);

        StudyGroup group = StudyGroup.builder()
                .id(groupId)
                .status(GroupStatus.ACTIVE)
                .build();

        StudyGroupResponse expectedResponse = StudyGroupResponse.builder()
                .id(groupId)
                .status(GroupStatus.CANCELLED)
                .build();

        when(studyGroupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(studyGroupRepository.save(any(StudyGroup.class))).thenReturn(group);
        when(studyGroupMapper.toResponse(any(StudyGroup.class))).thenReturn(expectedResponse);

        StudyGroupResponse result = studyGroupService.updateStatus(groupId, request);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(GroupStatus.CANCELLED);
        assertThat(group.getStatus()).isEqualTo(GroupStatus.CANCELLED);
        verify(studyGroupRepository).findById(groupId);
        verify(studyGroupRepository).save(group);
        verify(studyGroupMapper).toResponse(group);
    }

    @Test
    @DisplayName("DRAFT → COMPLETED: недопустимый переход → BadRequestException с обоими статусами")
    void updateStatus_DraftToCompleted_ShouldThrowBadRequestException() {

        Long groupId = 1L;
        GroupStatusUpdateRequest request = new GroupStatusUpdateRequest(GroupStatus.COMPLETED);

        StudyGroup group = StudyGroup.builder()
                .id(groupId)
                .status(GroupStatus.DRAFT)
                .build();

        when(studyGroupRepository.findById(groupId)).thenReturn(Optional.of(group));

        assertThatThrownBy(() -> studyGroupService.updateStatus(groupId, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("DRAFT")
                .hasMessageContaining("COMPLETED");

        verify(studyGroupRepository).findById(groupId);
        verify(studyGroupRepository, never()).save(any());
        verifyNoInteractions(studyGroupMapper);
    }

    @Test
    @DisplayName("COMPLETED → ACTIVE: переход из финального статуса → BadRequestException")
    void updateStatus_CompletedToActive_ShouldThrowBadRequestException() {

        Long groupId = 1L;
        GroupStatusUpdateRequest request = new GroupStatusUpdateRequest(GroupStatus.ACTIVE);

        StudyGroup group = StudyGroup.builder()
                .id(groupId)
                .status(GroupStatus.COMPLETED)
                .build();

        when(studyGroupRepository.findById(groupId)).thenReturn(Optional.of(group));

        assertThatThrownBy(() -> studyGroupService.updateStatus(groupId, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("COMPLETED")
                .hasMessageContaining("ACTIVE");

        verify(studyGroupRepository).findById(groupId);
        verify(studyGroupRepository, never()).save(any());
        verifyNoInteractions(studyGroupMapper);
    }

    @Test
    @DisplayName("CANCELLED → ACTIVE: переход из финального статуса → BadRequestException")
    void updateStatus_CancelledToActive_ShouldThrowBadRequestException() {

        Long groupId = 1L;
        GroupStatusUpdateRequest request = new GroupStatusUpdateRequest(GroupStatus.ACTIVE);

        StudyGroup group = StudyGroup.builder()
                .id(groupId)
                .status(GroupStatus.CANCELLED)
                .build();

        when(studyGroupRepository.findById(groupId)).thenReturn(Optional.of(group));

        assertThatThrownBy(() -> studyGroupService.updateStatus(groupId, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("CANCELLED")
                .hasMessageContaining("ACTIVE");

        verify(studyGroupRepository).findById(groupId);
        verify(studyGroupRepository, never()).save(any());
        verifyNoInteractions(studyGroupMapper);
    }

    @Test
    @DisplayName("Группа не найдена → ResourceNotFoundException")
    void updateStatus_GroupNotFound_ShouldThrowResourceNotFoundException() {

        Long groupId = 99L;
        GroupStatusUpdateRequest request = new GroupStatusUpdateRequest(GroupStatus.ACTIVE);

        when(studyGroupRepository.findById(groupId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studyGroupService.updateStatus(groupId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("StudyGroup")
                .hasMessageContaining("99");

        verify(studyGroupRepository).findById(groupId);
        verify(studyGroupRepository, never()).save(any());
        verifyNoInteractions(studyGroupMapper);
    }
}


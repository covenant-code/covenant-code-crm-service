package com.covenantcode.crm.service.impl;

import com.covenantcode.crm.dto.group.StudyGroupCreateRequest;
import com.covenantcode.crm.dto.group.StudyGroupResponse;
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
    private StudyGroup group1, group2;
    private StudyGroupResponse resp1, resp2;

    @BeforeEach
    void setUp() {
        group1 = new StudyGroup();
        group1.setId(1L);
        group1.setName("Java Core");
        group1.setStatus(GroupStatus.ACTIVE);

        group2 = new StudyGroup();
        group2.setId(2L);
        group2.setName("Spring Boot");
        group2.setStatus(GroupStatus.DRAFT);

        resp1 = new StudyGroupResponse();
        resp1.setId(1L);
        resp1.setName("Java Core");
        resp1.setStatus(GroupStatus.ACTIVE);

        resp2 = new StudyGroupResponse();
        resp2.setId(2L);
        resp2.setName("Spring Boot");
        resp2.setStatus(GroupStatus.DRAFT);
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
}


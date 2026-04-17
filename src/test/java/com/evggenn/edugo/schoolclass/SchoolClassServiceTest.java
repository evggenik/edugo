package com.evggenn.edugo.schoolclass;

import com.evggenn.edugo.exception.*;
import com.evggenn.edugo.user.Role;
import com.evggenn.edugo.user.User;
import com.evggenn.edugo.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SchoolClassServiceTest {

    @Mock
    private SchoolClassRepository schoolClassRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SchoolClassMapper mapper; // required by @InjectMocks — SchoolClassService depends on it

    @InjectMocks
    private SchoolClassService schoolClassService;

    private static final String CURRENT_YEAR = "2025-2026";

    @Test
    void createClass_shouldSaveClassWithoutTeacher_whenTeacherIdNotProvided() {

        SchoolClassCreateRequest request = new SchoolClassCreateRequest("7Б", null);

        when(schoolClassRepository.existsByNameAndAcademicYear("7Б", CURRENT_YEAR)).thenReturn(false);
        when(schoolClassRepository.save(any(SchoolClass.class))).thenReturn(new SchoolClass("7Б", CURRENT_YEAR));

        ArgumentCaptor<SchoolClass> captor = ArgumentCaptor.forClass(SchoolClass.class);

        schoolClassService.createClass(request);

        verify(schoolClassRepository).save(captor.capture());
        SchoolClass saved = captor.getValue();

        assertThat(saved.getName()).isEqualTo("7Б");
        assertThat(saved.getAcademicYear()).isEqualTo(CURRENT_YEAR);
        assertThat(saved.getTeacher()).isNull();
        verify(userRepository, never()).findById(any());
    }

    @Test
    void createClass_shouldCreateClassWithTeacher_whenTeacherIdProvided() {
        User teacher = User.builder()
                .id(1L)
                .firstName("Марья")
                .lastName("Ивановна")
                .email("teacher@teacher.com")
                .password("password")
                .roles(Set.of())
                .build();
        SchoolClassCreateRequest request = new SchoolClassCreateRequest("7Б", teacher.getId());

        when(schoolClassRepository.existsByNameAndAcademicYear("7Б", CURRENT_YEAR)).thenReturn(false);
        when(userRepository.findById(request.teacherId())).thenReturn(Optional.of(teacher));
        when(schoolClassRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        //when(schoolClassRepository.save(any(SchoolClass.class))).thenReturn(new SchoolClass("7Б", CURRENT_YEAR));

        schoolClassService.createClass(request);
        ArgumentCaptor<SchoolClass> captor = ArgumentCaptor.forClass(SchoolClass.class);
        verify(schoolClassRepository).save(captor.capture());
        SchoolClass saved = captor.getValue();

        assertThat(saved.getTeacher()).isEqualTo(teacher);
        assertThat(saved.getName()).isEqualTo("7Б");
        assertThat(saved.getAcademicYear()).isEqualTo(CURRENT_YEAR);
        verify(userRepository).findById(teacher.getId());
        verify(schoolClassRepository).existsByNameAndAcademicYear("7Б", CURRENT_YEAR);
    }

    @Test
    void createClass_shouldThrowSchoolClassAlreadyExistsException() {
        SchoolClassCreateRequest request = new SchoolClassCreateRequest("7Б", null);

        when(schoolClassRepository.existsByNameAndAcademicYear("7Б", CURRENT_YEAR)).thenReturn(true);

        assertThatThrownBy(() -> schoolClassService.createClass(request))
                .isInstanceOf(SchoolClassAlreadyExistsException.class)
                .hasMessageContaining("7Б");
        verify(schoolClassRepository, never()).save(any());
    }


    @Test
    void updateClass_shouldThrowClassIsArchivedException_whenNotCurrentYearProvided() {

        SchoolClass updatedClass = new SchoolClass("7Б", "2024-2025");
        SchoolClassUpdateRequest request = new SchoolClassUpdateRequest("7Б", 1L);

        when(schoolClassRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(updatedClass));

        assertThatThrownBy(() -> schoolClassService.updateClass(1L, request))
                .isInstanceOf(ClassIsArchivedException.class);
        verify(schoolClassRepository).findByIdWithDetails(1L);
        verify(schoolClassRepository, never()).save(any());
        verifyNoInteractions(userRepository);
    }

    @Test
    void updateClass_shouldThrowSchoolClassAlreadyExistsException_whenClassWithProvidedNameExists() {

        SchoolClass updatedClass = new SchoolClass("7Б", "2025-2026");
        SchoolClassUpdateRequest request = new SchoolClassUpdateRequest("8Б", 1L);

        when(schoolClassRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(updatedClass));
        when(schoolClassRepository.existsByNameAndAcademicYear("8Б", "2025-2026")).thenReturn(true);

        assertThatThrownBy(() -> schoolClassService.updateClass(1L, request))
                .isInstanceOf(SchoolClassAlreadyExistsException.class);
        verify(schoolClassRepository).findByIdWithDetails(1L);
        verify(schoolClassRepository, never()).save(any());
        verifyNoInteractions(userRepository);
    }

    @Test
    void updateClass_shouldNotThrow_whenSameNameAndYear() {
        SchoolClass updatedClass = new SchoolClass("7Б", "2025-2026");
        SchoolClassUpdateRequest request = new SchoolClassUpdateRequest("7Б", 1L);
        User user = User.builder()
                .id(1L)
                .firstName("Марья Ивановна")
                .build();

        when(schoolClassRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(updatedClass));
        when(schoolClassRepository.existsByNameAndAcademicYear("7Б", "2025-2026")).thenReturn(true);
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        assertDoesNotThrow(() -> schoolClassService.updateClass(1L, request));
        assertThat(updatedClass.getTeacher()).isNotNull();
        assertThat(updatedClass.getTeacher().getId()).isEqualTo(1L);


        verify(schoolClassRepository).findByIdWithDetails(1L);
        verify(schoolClassRepository).existsByNameAndAcademicYear("7Б", "2025-2026");
        verify(userRepository).findById(1L);
    }

    @Test
    void updateClass_shouldUpdateClass_whenTeacherIdProvided() {
        SchoolClass updatedClass = new SchoolClass("7Б", "2025-2026");
        SchoolClassUpdateRequest request = new SchoolClassUpdateRequest("7Б", 1L);
        User teacher = User.builder()
                .id(1L)
                .firstName("Марья Ивановна")
                .build();

        when(schoolClassRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(updatedClass));
        when(schoolClassRepository.existsByNameAndAcademicYear("7Б", "2025-2026"))
                .thenReturn(false);
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(teacher));

        schoolClassService.updateClass(1L, request);

        assertThat(updatedClass.getTeacher()).isNotNull();
        assertThat(updatedClass.getTeacher().getId()).isEqualTo(1L);

        verify(schoolClassRepository).findByIdWithDetails(1L);
        verify(schoolClassRepository).existsByNameAndAcademicYear("7Б", "2025-2026");
        verify(userRepository).findById(1L);

    }




    @Test
    void addStudentToClass_shouldThrowUserNotFoundException_whenUserNotFound() {
        Long classId = 1L;
        Long studentId = 1L;
        SchoolClass updatedClass = new SchoolClass("7Б", "2025-2026");

        when(schoolClassRepository.findByIdWithDetails(classId)).thenReturn(Optional.of(updatedClass));
        when(userRepository.findByIdWithRoles(studentId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> schoolClassService.addStudentToClass(classId, studentId))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository).findByIdWithRoles(studentId);
        verify(schoolClassRepository).findByIdWithDetails(classId);
        verify(schoolClassRepository, never())
                .existsByStudentIdAndAcademicYear(studentId, "2025-2026");
    }

    @Test
    void addStudentToClass_shouldThrowNotStudentException_whenUserIsNotStudent() {
        Long classId = 1L;
        Long studentId = 1L;
        SchoolClass updatedClass = new SchoolClass("7Б", "2025-2026");
        User stranger = User.builder()
                .id(1L)
                .firstName("Вася")
                .lastName("Васильков")
                .roles(Set.of(Role.builder().name(Role.TEACHER).build()))
                .build();

        when(schoolClassRepository.findByIdWithDetails(classId)).thenReturn(Optional.of(updatedClass));
        when(userRepository.findByIdWithRoles(studentId))
                .thenReturn(Optional.of(stranger));

        assertThatThrownBy(() -> schoolClassService.addStudentToClass(classId, studentId))
                .isInstanceOf(NotStudentException.class);

        verify(schoolClassRepository).findByIdWithDetails(classId);
        verify(userRepository).findByIdWithRoles(studentId);
        verify(schoolClassRepository, never())
                .existsByStudentIdAndAcademicYear(studentId, "2025-2026");
    }

    @Test
    void addStudentToClass_shouldThrowStudentAlreadyInClassException_whenStudentExistsForThisYear() {
        Long classId = 1L;
        Long studentId = 1L;
        SchoolClass schoolClass = new SchoolClass("7Б", "2025-2026");
        User student = User.builder()
                .id(1L)
                .roles(Set.of(Role.builder().name(Role.STUDENT).build()))
                .build();

        when(schoolClassRepository.findByIdWithDetails(classId))
                .thenReturn(Optional.of(schoolClass));
        when(userRepository.findByIdWithRoles(studentId))
                .thenReturn(Optional.of(student));
        when(schoolClassRepository.existsByStudentIdAndAcademicYear(studentId, "2025-2026"))
                .thenReturn(true);

        assertThatThrownBy(() -> schoolClassService.addStudentToClass(classId, studentId))
                .isInstanceOf(StudentAlreadyInClassException.class);

        verify(schoolClassRepository).findByIdWithDetails(classId);
        verify(userRepository).findByIdWithRoles(studentId);
        verify(schoolClassRepository)
                .existsByStudentIdAndAcademicYear(studentId, "2025-2026");
    }

    @Test
    void addStudentToClass_shouldAddStudent() {
        Long classId = 1L;
        Long studentId = 1L;
        SchoolClass schoolClass = new SchoolClass("7Б", "2025-2026");
        User student = User.builder()
                .id(1L)
                .roles(Set.of(Role.builder().name(Role.STUDENT).build()))
                .build();

        when(schoolClassRepository.findByIdWithDetails(classId))
                .thenReturn(Optional.of(schoolClass));
        when(userRepository.findByIdWithRoles(studentId))
                .thenReturn(Optional.of(student));
        when(schoolClassRepository.existsByStudentIdAndAcademicYear(studentId, "2025-2026"))
                .thenReturn(false);

        schoolClassService.addStudentToClass(classId, studentId);

        assertThat(schoolClass.getStudents()).hasSize(1);
        assertThat(schoolClass.getStudents()).contains(student);
    }

    @Test
    void removeStudentFromClass_shouldThrowUserNotFoundException() {
        Long classId = 1L;
        Long studentId = 1L;

        when(userRepository.findById(studentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> schoolClassService.removeStudentFromClass(classId, studentId))
                .isInstanceOf(UserNotFoundException.class);

        verify(schoolClassRepository, never()).findByIdWithDetails(classId);
    }

    @Test
    void removeStudentFromClass_shouldRemoveStudent() {
        Long classId = 1L;
        SchoolClass schoolClass = new SchoolClass("7Б", "2025-2026");
        Long studentId = 1L;
        User student = User.builder()
                .id(1L)
                .build();

        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(schoolClassRepository.findByIdWithDetails(classId))
                .thenReturn(Optional.of(schoolClass));

        schoolClassService.removeStudentFromClass(classId, studentId);

        assertThat(schoolClass.getStudents()).hasSize(0);
    }

    @Test
    void getSchoolClass_shouldNotThrow_whenClassExists() {
        Long classId = 1L;
        SchoolClass schoolClass = new SchoolClass("7Б", "2025-2026");

        when(schoolClassRepository.findByIdWithDetails(classId))
                .thenReturn(Optional.of(schoolClass));

        assertDoesNotThrow(() -> schoolClassService.getSchoolClass(classId));
    }

    @Test
    void getSchoolClass_shouldThrow_whenClassNotFound() {
        Long classId = 1L;

        when(schoolClassRepository.findByIdWithDetails(classId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> schoolClassService.getSchoolClass(classId))
                .isInstanceOf(SchoolClassNotFoundException.class);
    }

    @Test
    void getAllClasses() {
        SchoolClass schoolClass = new SchoolClass("7Б", "2025-2026");

        when(schoolClassRepository.findAllByYearWithTeacher("2025-2026")).thenReturn(List.of(schoolClass));

        assertDoesNotThrow(() -> schoolClassService.getAllClasses("2025-2026"));
    }
}
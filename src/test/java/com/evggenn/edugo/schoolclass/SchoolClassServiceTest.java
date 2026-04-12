package com.evggenn.edugo.schoolclass;

import com.evggenn.edugo.exception.SchoolClassAlreadyExistsException;
import com.evggenn.edugo.user.User;
import com.evggenn.edugo.user.UserRepository;
import com.evggenn.edugo.util.AcademicYearUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SchoolClassServiceTest {

    @Mock
    private SchoolClassRepository schoolClassRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SchoolClassMapper mapper;

    @InjectMocks
    private SchoolClassService schoolClassService;

    private static final String CURRENT_YEAR = "2025-2026";

    @Test
    void createClass_shouldSaveClassWithoutTeacher_whenTeacherIdNotProvided() {
        String currentYear = AcademicYearUtil.getCurrentAcademicYear();
        SchoolClassCreateRequest request = new SchoolClassCreateRequest("7Б", null);

        when(schoolClassRepository.existsByNameAndAcademicYear("7Б", currentYear)).thenReturn(false);
        when(schoolClassRepository.save(any(SchoolClass.class))).thenReturn(new SchoolClass("7Б", currentYear));

        ArgumentCaptor<SchoolClass> captor = ArgumentCaptor.forClass(SchoolClass.class);

        schoolClassService.createClass(request);

        verify(schoolClassRepository).save(captor.capture());
        SchoolClass saved = captor.getValue();

        assertThat(saved.getName()).isEqualTo("7Б");
        assertThat(saved.getAcademicYear()).isEqualTo(currentYear);
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
    void updateClass() {
        // Given

        // When

        //Then

    }

    @Test
    void addStudentToClass() {
        // Given

        // When

        //Then

    }

    @Test
    void removeStudentFromClass() {
        // Given

        // When

        //Then

    }

    @Test
    void getSchoolClass() {
        // Given

        // When

        //Then

    }

    @Test
    void getAllClasses() {
        // Given

        // When

        //Then

    }
}
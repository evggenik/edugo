package com.evggenn.edugo.homework;

import com.evggenn.edugo.homework.exception.HomeworkAlreadyExistsException;
import com.evggenn.edugo.homework.exception.LessonCancelledException;
import com.evggenn.edugo.lesson.Lesson;
import com.evggenn.edugo.lesson.LessonRepository;
import com.evggenn.edugo.lesson.LessonStatus;
import com.evggenn.edugo.lesson.exception.LessonNotFoundException;
import com.evggenn.edugo.user.User;
import com.evggenn.edugo.util.AcademicYearUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class HomeworkServiceTest {

    @Mock
    private HomeworkRepository homeworkRepository;

    @Mock
    private LessonRepository lessonRepository;

    @InjectMocks
    private HomeworkService homeworkService;

    @Test
    void createHomework_shouldCreateHomework_whenDataIsValid() {
        String description = "Учебник, стр.666, упр.999";
        LocalDate dueDate = LocalDate.of(2026, 1, 2);
        Long lessonId = 1L;
        String academicYear = AcademicYearUtil.getCurrentAcademicYear();
        Long currentUserId = 11L;

        User teacher = User.builder().id(11L).build();

        Lesson lesson = Lesson.builder()
                .teacher(teacher)
                .status(LessonStatus.SCHEDULED)
                .build();

        when(lessonRepository.findByIdAndAcademicYear(lessonId, academicYear))
                .thenReturn(Optional.of(lesson));
        when(homeworkRepository.existsByLessonId(lessonId)).thenReturn(false);
        when(homeworkRepository.save(any(Homework.class)))
                .thenAnswer(i -> i.getArguments()[0]);

        Homework result = homeworkService.createHomework(
                description, dueDate, lessonId, academicYear, currentUserId
        );

        assertThat(result.getDescription()).isEqualTo(description);
        assertThat(result.getDueDate()).isEqualTo(dueDate);
        assertThat(result.getLesson()).isEqualTo(lesson);

        verify(homeworkRepository).existsByLessonId(lessonId);
        verify(homeworkRepository).save(any(Homework.class));
    }

    @Test
    void createHomework_shouldThrow_whenLessonNotFound() {
        Long lessonId = 1L;
        String academicYear = AcademicYearUtil.getCurrentAcademicYear();

        when(lessonRepository.findByIdAndAcademicYear(lessonId, academicYear))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> homeworkService.createHomework(
                "Учебник, стр.666, упр.999",
                LocalDate.of(2026, 1, 2),
                lessonId,
                academicYear,
                1L
        )).isInstanceOf(LessonNotFoundException.class);

        verify(lessonRepository)
                .findByIdAndAcademicYear(lessonId, academicYear);
        verifyNoInteractions(homeworkRepository);
    }

    @Test
    void createHomework_shouldThrow_whenHomeworkExists() {
        Long lessonId = 1L;
        String academicYear = AcademicYearUtil.getCurrentAcademicYear();
        Lesson lesson = Lesson.builder().id(lessonId).build();

        when(lessonRepository.findByIdAndAcademicYear(lessonId, academicYear))
                .thenReturn(Optional.of(lesson));
        when(homeworkRepository.existsByLessonId(lessonId)).thenReturn(true);

        assertThatThrownBy(() -> homeworkService.createHomework(
                "Учебник, стр.666, упр.999",
                LocalDate.of(2026, 1, 2),
                lessonId,
                academicYear,
                1L
        )).isInstanceOf(HomeworkAlreadyExistsException.class);

        verify(lessonRepository)
                .findByIdAndAcademicYear(lessonId, academicYear);
        verify(homeworkRepository).existsByLessonId(lessonId);
        verifyNoMoreInteractions(homeworkRepository);
    }

    @Test
    void createHomework_shouldThrow_whenCurrentUserIsNotLessonTeacher() {
        Long lessonId = 1L;
        String academicYear = AcademicYearUtil.getCurrentAcademicYear();
        User teacher = User.builder().id(1L).build();
        Lesson lesson = Lesson.builder()
                .teacher(teacher)
                .build();
        Long currentUserId = 2L;

        when(lessonRepository.findByIdAndAcademicYear(lessonId, academicYear))
                .thenReturn(Optional.of(lesson));
        when(homeworkRepository.existsByLessonId(lessonId)).thenReturn(false);

        assertThatThrownBy(() -> homeworkService.createHomework(
                "Учебник, стр.666, упр.999",
                LocalDate.of(2026, 1, 2),
                lessonId,
                academicYear,
                currentUserId
        )).isInstanceOf(AccessDeniedException.class);

        verify(lessonRepository)
                .findByIdAndAcademicYear(lessonId, academicYear);
        verify(homeworkRepository).existsByLessonId(lessonId);
        verifyNoMoreInteractions(homeworkRepository);
    }

    @Test
    void createHomework_shouldThrow_whenLessonIsCancelled() {
        Long lessonId = 1L;
        String academicYear = AcademicYearUtil.getCurrentAcademicYear();
        User teacher = User.builder().id(1L).build();
        Lesson lesson = Lesson.builder()
                .teacher(teacher)
                .status(LessonStatus.CANCELLED)
                .build();
        Long currentUserId = 1L;

        when(lessonRepository.findByIdAndAcademicYear(lessonId, academicYear))
                .thenReturn(Optional.of(lesson));
        when(homeworkRepository.existsByLessonId(lessonId)).thenReturn(false);

        assertThatThrownBy(() -> homeworkService.createHomework(
                "Учебник, стр.666, упр.999",
                LocalDate.of(2026, 1, 2),
                lessonId,
                academicYear,
                currentUserId
        )).isInstanceOf(LessonCancelledException.class);

        verify(lessonRepository)
                .findByIdAndAcademicYear(lessonId, academicYear);
        verify(homeworkRepository).existsByLessonId(lessonId);
        verifyNoMoreInteractions(homeworkRepository);
    }

    @Test
    void updateHomework() {
        // Given

        // When

        //Then

    }

    @Test
    void getHomework() {
        // Given

        // When

        //Then

    }

    @Test
    void getHomeworkByLesson() {
        // Given

        // When

        //Then

    }
}
package com.evggenn.edugo.homework;

import com.evggenn.edugo.homework.exception.HomeworkAlreadyExistsException;
import com.evggenn.edugo.homework.exception.HomeworkNotFoundException;
import com.evggenn.edugo.homework.exception.InvalidDueDateException;
import com.evggenn.edugo.homework.exception.LessonCancelledException;
import com.evggenn.edugo.lesson.Lesson;
import com.evggenn.edugo.lesson.LessonRepository;
import com.evggenn.edugo.lesson.LessonStatus;
import com.evggenn.edugo.lesson.exception.LessonNotFoundException;
import com.evggenn.edugo.term.Term;
import com.evggenn.edugo.user.User;
import com.evggenn.edugo.util.AcademicYearUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
        LocalDate dueDate = LocalDate.of(2026, 1, 21);
        Long lessonId = 1L;
        String academicYear = AcademicYearUtil.getCurrentAcademicYear();
        Long currentUserId = 11L;

        User teacher = User.builder().id(11L).build();

        Term term = new Term(
                "2 четверть",
                LocalDate.of(2026, 1, 10),
                LocalDate.of(2026, 3, 20),
                academicYear
        );

        Lesson lesson = Lesson.builder()
                .teacher(teacher)
                .status(LessonStatus.SCHEDULED)
                .startTime(LocalDateTime.of(2026, 1, 12, 8, 30))
                .term(term)
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
    void createHomework_shouldThrow_whenDueDateIsBeforeLessonDate() {
        LocalDate dueDate = LocalDate.of(2026, 1, 10);
        Long lessonId = 1L;
        String academicYear = AcademicYearUtil.getCurrentAcademicYear();
        Term term = new Term(
                "2 четверть",
                LocalDate.of(2026, 1, 10),
                LocalDate.of(2026, 3, 20),
                academicYear
        );
        Lesson lesson = Lesson.builder()
                .id(lessonId)
                .startTime(LocalDateTime.of(2026, 1, 12, 8, 30))
                .term(term)
                .build();

        when(lessonRepository.findByIdAndAcademicYear(lessonId, academicYear))
                .thenReturn(Optional.of(lesson));

        assertThatThrownBy(() -> homeworkService.createHomework(
                "Учебник, стр.666, упр.999",
                dueDate,
                lessonId,
                academicYear,
                null
        ))
                .isInstanceOf(InvalidDueDateException.class)
                .extracting(
                        ex -> ((InvalidDueDateException) ex).getReason())
                .isEqualTo(InvalidDueDateException.Reason.BEFORE_LESSON);


        verifyNoInteractions(homeworkRepository);
    }

    @Test
    void createHomework_shouldThrow_whenDueDateIsAfterTermEnd() {
        LocalDate dueDate = LocalDate.of(2026, 3, 21);
        Long lessonId = 1L;
        String academicYear = AcademicYearUtil.getCurrentAcademicYear();
        Term term = new Term(
                "2 четверть",
                LocalDate.of(2026, 1, 10),
                LocalDate.of(2026, 3, 20),
                academicYear
        );
        Lesson lesson = Lesson.builder()
                .id(lessonId)
                .startTime(LocalDateTime.of(2026, 1, 12, 8, 30))
                .term(term)
                .build();

        when(lessonRepository.findByIdAndAcademicYear(lessonId, academicYear))
                .thenReturn(Optional.of(lesson));

        assertThatThrownBy(() -> homeworkService.createHomework(
                "Учебник, стр.666, упр.999",
                dueDate,
                lessonId,
                academicYear,
                null
        ))
                .isInstanceOf(InvalidDueDateException.class)
                .extracting(
                        ex -> ((InvalidDueDateException) ex).getReason())
                .isEqualTo(InvalidDueDateException.Reason.AFTER_TERM_END);


        verifyNoInteractions(homeworkRepository);
    }

    @Test
    void createHomework_shouldThrow_whenHomeworkExists() {
        Long lessonId = 1L;
        String academicYear = AcademicYearUtil.getCurrentAcademicYear();

        Term term = new Term(
                "2 четверть",
                LocalDate.of(2026, 1, 10),
                LocalDate.of(2026, 3, 20),
                academicYear
        );

        Lesson lesson = Lesson.builder().
                id(lessonId)
                .startTime(LocalDateTime.of(2026, 1, 12, 8, 30))
                .term(term)
                .build();

        when(lessonRepository.findByIdAndAcademicYear(lessonId, academicYear))
                .thenReturn(Optional.of(lesson));
        when(homeworkRepository.existsByLessonId(lessonId)).thenReturn(true);

        assertThatThrownBy(() -> homeworkService.createHomework(
                "Учебник, стр.666, упр.999",
                LocalDate.of(2026, 1, 21),
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

        Term term = new Term(
                "2 четверть",
                LocalDate.of(2026, 1, 10),
                LocalDate.of(2026, 3, 20),
                academicYear
        );

        Lesson lesson = Lesson.builder()
                .teacher(teacher)
                .startTime(LocalDateTime.of(2026, 1, 12, 8, 30))
                .term(term)
                .build();
        Long currentUserId = 2L;

        when(lessonRepository.findByIdAndAcademicYear(lessonId, academicYear))
                .thenReturn(Optional.of(lesson));
        when(homeworkRepository.existsByLessonId(lessonId)).thenReturn(false);

        assertThatThrownBy(() -> homeworkService.createHomework(
                "Учебник, стр.666, упр.999",
                LocalDate.of(2026, 1, 21),
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

        Term term = new Term(
                "2 четверть",
                LocalDate.of(2026, 1, 10),
                LocalDate.of(2026, 3, 20),
                academicYear
        );

        Lesson lesson = Lesson.builder()
                .teacher(teacher)
                .startTime(LocalDateTime.of(2026, 1, 12, 8, 30))
                .status(LessonStatus.CANCELLED)
                .term(term)
                .build();
        Long currentUserId = 1L;

        when(lessonRepository.findByIdAndAcademicYear(lessonId, academicYear))
                .thenReturn(Optional.of(lesson));
        when(homeworkRepository.existsByLessonId(lessonId)).thenReturn(false);

        assertThatThrownBy(() -> homeworkService.createHomework(
                "Учебник, стр.666, упр.999",
                LocalDate.of(2026, 1, 21),
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
    void updateHomework_shouldUpdateDescriptionAndDueDate_whenBothProvided() {
        String newDescription = "Учебник, стр.555, упр.777";
        LocalDate newDueDate = LocalDate.of(2026, 2, 14);
        Long currentUserId = 11L;
        String academicYear = AcademicYearUtil.getCurrentAcademicYear();
        Term term = new Term(
                "2 четверть",
                LocalDate.of(2026, 1, 10),
                LocalDate.of(2026, 3, 20),
                academicYear
        );

        User teacher = User.builder().id(11L).build();

        Lesson lesson = Lesson.builder()
                .teacher(teacher)
                .startTime(LocalDateTime.of(2026, 2, 12, 8, 30))
                .status(LessonStatus.SCHEDULED)
                .term(term)
                .build();

        Homework homework = Homework.builder()
                .id(2L)
                .description("Учебник, стр.666, упр.999")
                .dueDate(LocalDate.of(2026, 2, 13))
                .lesson(lesson)
                .build();

        when(homeworkRepository.findByIdWithDetails(homework.getId()))
                .thenReturn(Optional.of(homework));

        homeworkService.updateHomework(
                2L,
                newDescription,
                newDueDate,
                currentUserId
        );

        assertThat(homework.getDescription()).isEqualTo(newDescription);
        assertThat(homework.getDueDate()).isEqualTo(newDueDate);

        verify(homeworkRepository).findByIdWithDetails(homework.getId());
        verifyNoMoreInteractions(homeworkRepository);
    }

    @Test
    void updateHomework_shouldUpdateOnlyDescription_whenDueDateIsNull() {
        String newDescription = "Учебник, стр.555, упр.777";
        LocalDate newDueDate = null;
        LocalDate oldDueDate = LocalDate.of(2026, 1, 2);
        Long currentUserId = 11L;

        User teacher = User.builder().id(11L).build();

        Lesson lesson = Lesson.builder()
                .teacher(teacher)
                .status(LessonStatus.SCHEDULED)
                .build();

        Homework homework = Homework.builder()
                .id(2L)
                .description("Учебник, стр.666, упр.999")
                .dueDate(oldDueDate)
                .lesson(lesson)
                .build();

        when(homeworkRepository.findByIdWithDetails(homework.getId()))
                .thenReturn(Optional.of(homework));

        homeworkService.updateHomework(
                2L,
                newDescription,
                newDueDate,
                currentUserId
        );

        assertThat(homework.getDescription()).isEqualTo(newDescription);
        assertThat(homework.getDueDate()).isEqualTo(oldDueDate);

        verify(homeworkRepository).findByIdWithDetails(homework.getId());
        verifyNoMoreInteractions(homeworkRepository);
    }

    @Test
    void updateHomework_shouldUpdateOnlyDueDate_whenDescriptionIsNull() {
        String newDescription = null;
        String oldDescription = "Учебник, стр.555, упр.777";
        LocalDate oldDueDate = LocalDate.of(2026, 1, 2);
        LocalDate newDueDate = LocalDate.of(2026, 2, 5);
        Long currentUserId = 11L;
        String academicYear = AcademicYearUtil.getCurrentAcademicYear();

        User teacher = User.builder().id(11L).build();

        Term term = new Term(
                "2 четверть",
                LocalDate.of(2026, 1, 10),
                LocalDate.of(2026, 3, 20),
                academicYear
        );

        Lesson lesson = Lesson.builder()
                .teacher(teacher)
                .status(LessonStatus.SCHEDULED)
                .startTime(LocalDateTime.of(2026, 1, 12, 8, 30))
                .term(term)
                .build();

        Homework homework = Homework.builder()
                .id(2L)
                .description(oldDescription)
                .dueDate(oldDueDate)
                .lesson(lesson)
                .build();

        when(homeworkRepository.findByIdWithDetails(homework.getId()))
                .thenReturn(Optional.of(homework));

        homeworkService.updateHomework(
                2L,
                newDescription,
                newDueDate,
                currentUserId
        );

        assertThat(homework.getDescription()).isEqualTo(oldDescription);
        assertThat(homework.getDueDate()).isEqualTo(newDueDate);

        verify(homeworkRepository).findByIdWithDetails(homework.getId());
        verifyNoMoreInteractions(homeworkRepository);
    }

    @Test
    void updateHomework_shouldThrow_whenHomeworkNotFound() {
        Homework homework = Homework.builder()
                .id(2L)
                .build();

        when(homeworkRepository.findByIdWithDetails(homework.getId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> homeworkService.updateHomework(
                2L,
                null,
                null,
                null
        )).isInstanceOf(HomeworkNotFoundException.class);

        verify(homeworkRepository).findByIdWithDetails(homework.getId());
    }

    @Test
    void updateHomework_shouldThrow_whenCurrentUserIsNotLessonTeacher() {
        Long currentUserId = 11L;
        User teacher = User.builder().id(22L).build();

        Lesson lesson = Lesson.builder()
                .teacher(teacher)
                .build();

        Homework homework = Homework.builder()
                .id(2L)
                .lesson(lesson)
                .build();

        when(homeworkRepository.findByIdWithDetails(homework.getId()))
                .thenReturn(Optional.of(homework));

        assertThatThrownBy(() -> homeworkService.updateHomework(
                2L,
                null,
                null,
                currentUserId
        )).isInstanceOf(AccessDeniedException.class);

        verify(homeworkRepository).findByIdWithDetails(homework.getId());
    }

    @Test
    void updateHomework_shouldThrow_whenNewDueDateIsBeforeLesson() {
        String newDescription = "Учебник, стр.555, упр.777";
        LocalDate newDueDate = LocalDate.of(2026, 2, 11);
        Long currentUserId = 11L;
        Long lessonId = 22L;
        String academicYear = AcademicYearUtil.getCurrentAcademicYear();
        Term term = new Term(
                "2 четверть",
                LocalDate.of(2026, 1, 10),
                LocalDate.of(2026, 3, 20),
                academicYear
        );

        User teacher = User.builder().id(11L).build();

        Lesson lesson = Lesson.builder()
                .id(lessonId)
                .teacher(teacher)
                .startTime(LocalDateTime.of(2026, 2, 12, 8, 30))
                .status(LessonStatus.SCHEDULED)
                .term(term)
                .build();

        Homework homework = Homework.builder()
                .id(12L)
                .description("Учебник, стр.666, упр.999")
                .dueDate(LocalDate.of(2026, 2, 13))
                .lesson(lesson)
                .build();

        when(homeworkRepository.findByIdWithDetails(homework.getId()))
                .thenReturn(Optional.of(homework));

        assertThatThrownBy(() -> homeworkService.updateHomework(
                homework.getId(),
                newDescription,
                newDueDate,
                currentUserId
        ))
                .isInstanceOf(InvalidDueDateException.class)
                .extracting(
                        ex -> ((InvalidDueDateException) ex).getReason())
                .isEqualTo(InvalidDueDateException.Reason.BEFORE_LESSON);

        assertThat(homework.getDescription()).isEqualTo(newDescription);
        assertThat(homework.getDueDate()).isEqualTo(LocalDate.of(2026, 2, 13));

        verify(homeworkRepository).findByIdWithDetails(homework.getId());
        verifyNoMoreInteractions(homeworkRepository);
    }

    @Test
    void getHomework_shouldThrow_whenHomeworkNotFound() {
        Long id = 1L;

        when(homeworkRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> homeworkService.getHomework(id))
                .isInstanceOf(HomeworkNotFoundException.class);

        verify(homeworkRepository).findById(id);

    }

    @Test
    void getHomeworkByLesson_shouldThrow_whenHomeworkNotFound() {
        Long lessonId = 1L;

        when(homeworkRepository.findByLessonId(lessonId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> homeworkService.getHomeworkByLesson(lessonId))
                .isInstanceOf(HomeworkNotFoundException.class);

        verify(homeworkRepository).findByLessonId(lessonId);
    }
}
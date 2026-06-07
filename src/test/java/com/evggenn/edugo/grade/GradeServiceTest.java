package com.evggenn.edugo.grade;

import com.evggenn.edugo.grade.exception.InvalidFinalGradeException;
import com.evggenn.edugo.grade.exception.InvalidLessonGradeException;
import com.evggenn.edugo.lesson.Lesson;
import com.evggenn.edugo.lesson.LessonRepository;
import com.evggenn.edugo.lesson.exception.LessonNotFoundException;
import com.evggenn.edugo.subject.Subject;
import com.evggenn.edugo.subject.SubjectRepository;
import com.evggenn.edugo.subject.exception.SubjectNotFoundException;
import com.evggenn.edugo.term.Term;
import com.evggenn.edugo.term.TermRepository;
import com.evggenn.edugo.term.exception.TermNotFoundException;
import com.evggenn.edugo.user.User;
import com.evggenn.edugo.user.UserService;
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
class GradeServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private TermRepository termRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private GradeRepository gradeRepository;

    @InjectMocks
    private GradeService gradeService;

    private final String currentAcademicYear = AcademicYearUtil.getCurrentAcademicYear();

    @Test
    void createGrade_shouldReturnGrade_whenGradeTypeIsLesson() {
        Short value = 2;
        GradeType type = GradeType.LESSON;
        String comment = "опять двойка(";
        Long studentId = 1L;
        Long lessonId = 1L;
        Long termId = null;
        Long subjectId = null;
        Long currentUserId = 1L;

        User student = User.builder().id(studentId).build();
        Lesson lesson = Lesson.builder().id(lessonId).build();
        User teacher = User.builder().id(1L).build();
        Subject pe = new Subject("PE");
        lesson.setTeacher(teacher);
        lesson.setSubject(pe);

        when(userService.findStudentByIdOrThrow(studentId)).thenReturn(student);
        when(lessonRepository.findByIdWithSubject(lessonId)).thenReturn(Optional.of(lesson));
        when(gradeRepository.save(any(Grade.class)))
                .thenAnswer(i -> i.getArguments()[0]);

        Grade result = gradeService.createGrade(
                                                value,
                                                type,
                                                comment,
                                                studentId,
                                                lessonId,
                                                termId,
                                                subjectId,
                                                currentUserId);

        assertThat(result.getValue()).isEqualTo(value);
        assertThat(result.getComment()).isEqualTo(comment);
        assertThat(result.getStudent()).isEqualTo(student);
        assertThat(result.getLesson()).isEqualTo(lesson);
        assertThat(result.getSubject()).isEqualTo(pe);

        verify(gradeRepository).save(any(Grade.class));
        verify(termRepository, never()).findById(any());
        verify(subjectRepository, never()).findById(any());
    }

    @Test
    void createGrade_shouldThrow_whenLessonIdIsNull() {
        GradeType type = GradeType.LESSON;
        Long studentId = 1L;
        Long lessonId = null;

        User student = User.builder().id(studentId).build();

        when(userService.findStudentByIdOrThrow(studentId)).thenReturn(student);

        assertThatThrownBy(() ->
                gradeService.createGrade(
                        null,
                        type,
                        null,
                        studentId,
                        lessonId,
                        null,
                        null,
                        null
                )
        ).isInstanceOf(InvalidLessonGradeException.class);

        verify(userService).findStudentByIdOrThrow(studentId);
        verifyNoInteractions(
                            lessonRepository,
                            termRepository,
                            subjectRepository,
                            gradeRepository
        );
    }

    @Test
    void createGrade_shouldThrow_whenLessonNotFound() {
        GradeType type = GradeType.LESSON;
        Long studentId = 1L;
        Long lessonId = 10L;

        User student = User.builder().id(studentId).build();

        when(userService.findStudentByIdOrThrow(studentId)).thenReturn(student);
        when(lessonRepository.findByIdWithSubject(lessonId)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                gradeService.createGrade(
                        null,
                        type,
                        null,
                        studentId,
                        lessonId,
                        null,
                        null,
                        null
                )
        ).isInstanceOf(LessonNotFoundException.class)
                .hasMessageContaining(lessonId.toString());

        verify(userService).findStudentByIdOrThrow(studentId);
        verify(lessonRepository).findByIdWithSubject(lessonId);
        verifyNoInteractions(
                termRepository,
                subjectRepository,
                gradeRepository
        );
    }

    @Test
    void createGrade_shouldThrow_whenCurrentUserIsNotLessonTeacher() {
        GradeType type = GradeType.LESSON;
        Long studentId = 1L;
        Long lessonId = 10L;
        Lesson lesson = Lesson.builder().id(lessonId).build();
        User teacher = User.builder().id(2L).build();
        lesson.setTeacher(teacher);

        Long currentUserId = 3L;

        User student = User.builder().id(studentId).build();

        when(userService.findStudentByIdOrThrow(studentId)).thenReturn(student);
        when(lessonRepository.findByIdWithSubject(lessonId)).thenReturn(Optional.of(lesson));

        assertThatThrownBy(() ->
                gradeService.createGrade(
                        null,
                        type,
                        null,
                        studentId,
                        lessonId,
                        null,
                        null,
                        currentUserId
                )
        ).isInstanceOf(AccessDeniedException.class);

        verify(userService).findStudentByIdOrThrow(studentId);
        verify(lessonRepository).findByIdWithSubject(lessonId);

        verifyNoInteractions(
                termRepository,
                subjectRepository,
                gradeRepository
        );
    }

    @Test
    void createGrade_shouldReturnGrade_whenGradeTypeIsNotLesson() {
        Short value = 2;
        GradeType type = GradeType.QUARTER;
        String comment = "опять двойка(";
        Long studentId = 1L;
        Long lessonId = null;
        Long termId = 1L;
        Long subjectId = 1L;
        Long currentUserId = null;

        User student = User.builder().id(studentId).build();
        Subject pe = new Subject("PE");
        Term term4 = getTermOfCurrentYear();

        when(userService.findStudentByIdOrThrow(studentId)).thenReturn(student);
        when(termRepository.findById(termId)).thenReturn(Optional.of(term4));
        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(pe));

        when(gradeRepository.save(any(Grade.class)))
                .thenAnswer(i -> i.getArguments()[0]);

        Grade result = gradeService.createGrade(
                value,
                type,
                comment,
                studentId,
                lessonId,
                termId,
                subjectId,
                currentUserId);

        assertThat(result.getValue()).isEqualTo(value);
        assertThat(result.getComment()).isEqualTo(comment);
        assertThat(result.getStudent()).isEqualTo(student);
        assertThat(result.getSubject()).isEqualTo(pe);
        assertThat(result.getTerm()).isEqualTo(term4);
        assertThat(result.getLesson()).isNull();

        verify(gradeRepository).save(any(Grade.class));
        verify(lessonRepository, never()).findByIdWithSubject(any());
        verify(subjectRepository).findById(subjectId);
    }

    @Test
    void createGrade_shouldThrow_whenTermIdIsNull() {
        GradeType type = GradeType.QUARTER;
        Long studentId = 1L;

        User student = User.builder().id(studentId).build();

        when(userService.findStudentByIdOrThrow(studentId)).thenReturn(student);

        assertThatThrownBy(() ->
                gradeService.createGrade(
                        null,
                        type,
                        null,
                        studentId,
                        null,
                        null,
                        null,
                        null
                )
        ).isInstanceOf(InvalidFinalGradeException.class)
                .hasMessageContaining(type.toString());

        verify(userService).findStudentByIdOrThrow(studentId);
        verifyNoInteractions(
                lessonRepository,
                termRepository,
                subjectRepository,
                gradeRepository
        );
    }

    @Test
    void createGrade_shouldThrow_whenTermNotFound() {
        GradeType type = GradeType.QUARTER;
        Long studentId = 1L;
        Long termId = 1L;


        User student = User.builder().id(studentId).build();

        when(userService.findStudentByIdOrThrow(studentId)).thenReturn(student);
        when(termRepository.findById(termId)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                gradeService.createGrade(
                        null,
                        type,
                        null,
                        studentId,
                        null,
                        termId,
                        null,
                        null
                )
        ).isInstanceOf(TermNotFoundException.class)
                .hasMessageContaining(termId.toString());

        verify(userService).findStudentByIdOrThrow(studentId);
        verify(termRepository).findById(termId);
        verifyNoInteractions(
                lessonRepository,
                subjectRepository,
                gradeRepository
        );
    }

    @Test
    void createGrade_shouldThrow_whenSubjectNotFound() {
        GradeType type = GradeType.QUARTER;
        Long studentId = 1L;
        Long termId = 1L;
        Long subjectId = 1L;
        Term term4 = getTermOfCurrentYear();


        User student = User.builder().id(studentId).build();

        when(userService.findStudentByIdOrThrow(studentId)).thenReturn(student);
        when(termRepository.findById(termId)).thenReturn(Optional.of(term4));
        when(subjectRepository.findById(subjectId)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                gradeService.createGrade(
                        null,
                        type,
                        null,
                        studentId,
                        null,
                        termId,
                        subjectId,
                        null
                )
        ).isInstanceOf(SubjectNotFoundException.class)
                .hasMessageContaining(subjectId.toString());

        verify(userService).findStudentByIdOrThrow(studentId);
        verify(termRepository).findById(termId);
        verify(subjectRepository).findById(subjectId);

        verifyNoInteractions(
                lessonRepository,
                gradeRepository
        );
    }

    @Test
    void updateGrade() {
        // Given

        // When

        //Then

    }

    @Test
    void deleteGrade() {
        // Given

        // When

        //Then

    }

    @Test
    void getGradesBySubjectTermAndStudent() {
        // Given

        // When

        //Then

    }

    @Test
    void getGrade() {
        // Given

        // When

        //Then

    }

    private Term getTermOfCurrentYear() {
        return new Term(
                "4 четверть", LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 5, 30),
                currentAcademicYear);
    }
}
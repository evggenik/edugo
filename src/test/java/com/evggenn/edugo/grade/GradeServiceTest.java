package com.evggenn.edugo.grade;

import com.evggenn.edugo.grade.exception.GradeNotEditableException;
import com.evggenn.edugo.grade.exception.GradeNotFoundException;
import com.evggenn.edugo.grade.exception.InvalidFinalGradeException;
import com.evggenn.edugo.grade.exception.InvalidLessonGradeException;
import com.evggenn.edugo.lesson.Lesson;
import com.evggenn.edugo.lesson.LessonRepository;
import com.evggenn.edugo.lesson.LessonStatus;
import com.evggenn.edugo.lesson.exception.LessonNotFoundException;
import com.evggenn.edugo.subject.Subject;
import com.evggenn.edugo.subject.SubjectRepository;
import com.evggenn.edugo.subject.exception.SubjectNotFoundException;
import com.evggenn.edugo.term.Term;
import com.evggenn.edugo.term.TermRepository;
import com.evggenn.edugo.term.exception.TermNotFoundException;
import com.evggenn.edugo.user.Role;
import com.evggenn.edugo.user.RoleName;
import com.evggenn.edugo.user.User;
import com.evggenn.edugo.user.UserService;
import com.evggenn.edugo.util.AcademicYearUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

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
    void updateGrade_shouldUpdateValueAndComment_whenBothProvided() {
        Long gradeId = 1L;
        Long currentUserId = 1L;
        short newValue = 2;
        String newComment = "опять двойка(";

        User teacher = User.builder().id(currentUserId).build();

        Lesson lesson = Lesson.builder()
                .teacher(teacher)
                .status(LessonStatus.COMPLETED)
                .build();

        Grade grade = Grade.builder()
                .id(gradeId)
                .value((short) 5)
                .type(GradeType.LESSON)
                .comment("ну, ты францууус!")
                .gradedAt(null)
                .student(null)
                .lesson(lesson)
                .term(null)
                .subject(null)
                .build();

        when(gradeRepository.findById(gradeId)).thenReturn(Optional.of(grade));

        gradeService.updateGrade(gradeId, newValue, newComment, currentUserId);

        assertThat(grade.getValue()).isEqualTo(newValue);
        assertThat(grade.getComment()).isEqualTo(newComment);
        verify(gradeRepository).findById(gradeId);
    }

    @Test
    void updateGrade_shouldUpdateOnlyValue_whenCommentIsNull() {
        Long gradeId = 1L;
        Long currentUserId = 1L;
        short oldValue = 5;
        short newValue = 2;
        String oldComment = "ну, ты француз!";

        User teacher = User.builder().id(currentUserId).build();

        Lesson lesson = Lesson.builder()
                .teacher(teacher)
                .status(LessonStatus.COMPLETED)
                .build();

        Grade grade = Grade.builder()
                .id(gradeId)
                .value(oldValue)
                .type(GradeType.LESSON)
                .comment(oldComment)
                .gradedAt(null)
                .student(null)
                .lesson(lesson)
                .term(null)
                .subject(null)
                .build();

        when(gradeRepository.findById(gradeId)).thenReturn(Optional.of(grade));

        gradeService.updateGrade(gradeId, newValue, null, currentUserId);

        assertThat(grade.getValue()).isEqualTo(newValue);
        assertThat(grade.getComment()).isEqualTo(oldComment);
        verify(gradeRepository).findById(gradeId);
    }

    @Test
    void updateGrade_shouldUpdateOnlyComment_whenValueIsNull() {
        Long gradeId = 1L;
        Long currentUserId = 1L;
        short oldValue = 5;
        short newValue = 2;
        String oldComment = "ну, ты француз!";
        String newComment = "пэпэвотафа";

        User teacher = User.builder().id(currentUserId).build();

        Lesson lesson = Lesson.builder()
                .teacher(teacher)
                .status(LessonStatus.COMPLETED)
                .build();

        Grade grade = Grade.builder()
                .id(gradeId)
                .value(oldValue)
                .type(GradeType.LESSON)
                .comment(oldComment)
                .gradedAt(null)
                .student(null)
                .lesson(lesson)
                .term(null)
                .subject(null)
                .build();

        when(gradeRepository.findById(gradeId)).thenReturn(Optional.of(grade));

        gradeService.updateGrade(gradeId, null, newComment, currentUserId);

        assertThat(grade.getValue()).isEqualTo(oldValue);
        assertThat(grade.getComment()).isEqualTo(newComment);
        verify(gradeRepository).findById(gradeId);
    }

    @Test
    void updateGrade_shouldThrow_whenGradeNotFound() {
        Long gradeId = 2L;

        when(gradeRepository.findById(gradeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                gradeService.updateGrade(
                        gradeId, null, null, null))
                .isInstanceOf(GradeNotFoundException.class)
                .hasMessageContaining(gradeId.toString());;
        verify(gradeRepository).findById(gradeId);
    }

    @Test
    void updateGrade_shouldThrow_whenLessonStatusIsNotCompleted() {
        Long gradeId = 2L;
        Lesson lesson = Lesson.builder()
                        .status(LessonStatus.SCHEDULED)
                        .build();

        Grade grade = new Grade();
        grade.setType(GradeType.LESSON);
        grade.setLesson(lesson);

        when(gradeRepository.findById(gradeId)).thenReturn(Optional.of(grade));

        assertThatThrownBy(() ->
                gradeService.updateGrade(
                        gradeId, null, null, null))
                .isInstanceOf(GradeNotEditableException.class)
                .hasMessageContaining(lesson.getStatus().name());

        verify(gradeRepository).findById(gradeId);
    }

    @Test
    void updateGrade_shouldThrow_whenCurrentUserNotTeachesThisLesson() {
        Long currentUserId = 1L;
        Long gradeId = 2L;
        User teacher = User.builder().id(2L).build();
        Lesson lesson = Lesson.builder()
                .status(LessonStatus.COMPLETED)
                .teacher(teacher)
                .build();

        Grade grade = new Grade();
        grade.setType(GradeType.LESSON);
        grade.setLesson(lesson);

        when(gradeRepository.findById(gradeId)).thenReturn(Optional.of(grade));

        assertThatThrownBy(() ->
                gradeService.updateGrade(
                        gradeId, null, null, currentUserId))
                .isInstanceOf(AccessDeniedException.class);

        verify(gradeRepository).findById(gradeId);
    }

    // TODO: add updateGrade tests for final grade types (QUARTER, YEAR, EXAM)
    // when validateFinalGradeAccess is implemented

    @Test
    void deleteGrade_shouldDeleteLessonGrade_whenExists() {
        Long currentUserId = 1L;
        User teacher = User.builder().id(1L).build();
        Lesson lesson = Lesson.builder()
                .id(1L)
                .status(LessonStatus.COMPLETED)
                .teacher(teacher)
                .build();
        Grade grade = new Grade();
        grade.setId(1L);
        grade.setType(GradeType.LESSON);
        grade.setLesson(lesson);
        grade.setValue((short) 3);
        grade.setComment("wtf");

        when(gradeRepository.findById(grade.getId())).thenReturn(Optional.of(grade));

        gradeService.deleteGrade(grade.getId(), currentUserId);

        verify(gradeRepository).delete(grade);
        verifyNoMoreInteractions(gradeRepository);
    }

    @Test
    void deleteGrade_shouldThrow_whenGradeNotFound() {
        Long currentUserId = 1L;
        Long gradeId = 1L;

        when(gradeRepository.findById(gradeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                gradeService.deleteGrade(gradeId, currentUserId))
                .isInstanceOf(GradeNotFoundException.class)
                .hasMessageContaining(gradeId.toString());

        verify(gradeRepository).findById(gradeId);
        verifyNoMoreInteractions(gradeRepository);
    }

    @Test
    void getGradesBySubjectTermAndStudent_shouldReturnListOfGrades_whenStudentRequestsOwnGrades() {
        Long subjectId = 10L;
        Subject subject = new Subject("Russian");
        subject.setId(subjectId);

        Long termId = 100L;
        Term term4 = getTermOfCurrentYear();

        Long studentId = 1L;
        Long currentUserId = 1L;
        Role studentRole = Role.builder().name(RoleName.STUDENT).build();
        User student = User.builder().id(studentId).roles(Set.of(studentRole)).build();

        List<Grade> grades = List.of(
                Grade.builder().id(1L).value((short)3).build(),
                Grade.builder().id(2L).value((short)4).build()
        );

        Authentication auth = new UsernamePasswordAuthenticationToken(
                null, null, Set.of(new SimpleGrantedAuthority(RoleName.STUDENT.name())));
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));
        when(termRepository.findByIdAndAcademicYear(
                termId, AcademicYearUtil.getCurrentAcademicYear()))
                .thenReturn(Optional.of(term4));
        when(userService.findStudentByIdOrThrow(studentId)).thenReturn(student);
        when(gradeRepository.findAllWithDetailsBySubjectAndTermAndStudent(
                subjectId, termId, studentId
        )).thenReturn(grades);

        List<Grade> result = gradeService.getGradesBySubjectTermAndStudent(
                subjectId, termId, studentId, currentUserId);

        assertThat(result).isEqualTo(grades);
    }

    @Test
    void getGradesBySubjectTermAndStudent_shouldThrow_whenStudentRequestsOtherStudentGrades() {
        Long subjectId = 10L;
        Long termId = 100L;
        Long studentId = 1L;
        Long currentUserId = 2L;


        Authentication auth = new UsernamePasswordAuthenticationToken(
                null, null, Set.of(new SimpleGrantedAuthority(RoleName.STUDENT.name())));
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertThatThrownBy(() -> gradeService.getGradesBySubjectTermAndStudent(
                subjectId, termId, studentId, currentUserId
        )).isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("You can only view your own grades");

        verifyNoInteractions(
                subjectRepository, termRepository, userService, gradeRepository);
    }

    // TODO: add test for PARENT access check when parent_students relationship is mapped

    @Test
    void getGrade_shouldReturnGrade_whenStudentRequestsOwnGrade() {
        Long id = 1L;
        Long currentUserId = 1L;

        User student = User.builder().id(currentUserId).build();

        Grade grade = Grade.builder()
                .id(id)
                .value((short) 3)
                .comment("wtf")
                .student(student)
                .build();

        Authentication auth = new UsernamePasswordAuthenticationToken(
                null, null, Set.of(new SimpleGrantedAuthority(RoleName.STUDENT.name())));
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(gradeRepository.findByIdWithDetails(id)).thenReturn(Optional.of(grade));

        Grade result = gradeService.getGrade(id,  currentUserId);

        assertThat(result).isEqualTo(grade);
    }

    @Test
    void getGrade_shouldThrow_whenStudentRequestsOtherStudentGrade() {
        Long id = 1L;
        Long currentUserId = 1L;

        User student = User.builder().id(2L).build();

        Grade grade = Grade.builder()
                .id(id)
                .value((short) 3)
                .comment("wtf")
                .student(student)
                .build();

        Authentication auth = new UsernamePasswordAuthenticationToken(
                null, null, Set.of(new SimpleGrantedAuthority(RoleName.STUDENT.name())));
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(gradeRepository.findByIdWithDetails(id)).thenReturn(Optional.of(grade));

        assertThatThrownBy(() -> gradeService.getGrade(id, currentUserId))
                .isInstanceOf(AccessDeniedException.class);
    }

    private Term getTermOfCurrentYear() {
        return new Term(
                "4 четверть", LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 5, 30),
                currentAcademicYear);
    }
}
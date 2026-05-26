package com.evggenn.edugo.lesson;

import com.evggenn.edugo.lesson.exception.*;
import com.evggenn.edugo.schoolclass.SchoolClass;
import com.evggenn.edugo.schoolclass.SchoolClassRepository;
import com.evggenn.edugo.schoolclass.exception.ClassIsArchivedException;
import com.evggenn.edugo.schoolclass.exception.SchoolClassNotFoundException;
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
import com.evggenn.edugo.user.exception.NotTeacherException;
import com.evggenn.edugo.user.exception.TeacherDoesNotTeachSubjectException;
import com.evggenn.edugo.user.exception.UserNotFoundException;
import com.evggenn.edugo.util.AcademicYearUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class LessonServiceTest {

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private SchoolClassRepository schoolClassRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private TermRepository termRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private LessonService lessonService;

    private String currentAcademicYear;

    private static final LocalDateTime LESSON_START_TIME = LocalDateTime
            .of(2026, 5, 20, 8, 30, 0);

    private static final LocalDateTime LESSON_END_TIME = LocalDateTime
            .of(2026, 5, 20, 9, 10, 0);

    @BeforeEach
    void setUp() {
         currentAcademicYear = AcademicYearUtil.getCurrentAcademicYear();
    }

    @Test
    void createLesson_shouldReturnLesson_whenDataIsValid() {
        String topic = "Maths";
        String room = "666";
        SchoolClass schoolClass = getSchoolClass();
        Long schoolClassId = schoolClass.getId();
        User teacher = getMathsTeacher();
        Long teacherId = teacher.getId();
        Subject maths = new Subject("Maths");
        maths.setId(1L);
        Long subjectId = maths.getId();
        Term term = getTermOfCurrentYear();
        Long termId = term.getId();


        when(schoolClassRepository.findById(schoolClassId)).thenReturn(Optional.of(schoolClass));
        when(userService.findTeacherByIdOrThrow(1L)).thenReturn(teacher);
        when(subjectRepository.findById(1L)).thenReturn(Optional.of(maths));
        when(termRepository.findByIdAndAcademicYear(
                termId, currentAcademicYear)).thenReturn(Optional.of(term));
        when(lessonRepository.existsOverlappingTimes(
                schoolClassId, LESSON_START_TIME, LESSON_END_TIME)).thenReturn(false);
        when(lessonRepository.existsOverlappingByTeacher(
                teacherId, LESSON_START_TIME, LESSON_END_TIME)).thenReturn(false);
        when(lessonRepository.save(any(Lesson.class)))
                .thenAnswer(i -> i.getArgument(0));

        Lesson result = lessonService.createLesson(
                topic, LESSON_START_TIME, LESSON_END_TIME, room, schoolClassId,
                subjectId, teacherId, termId);

        assertThat(result.getTopic()).isEqualTo(topic);
        assertThat(result.getStartTime()).isEqualTo(LESSON_START_TIME);
        assertThat(result.getSchoolClass()).isEqualTo(schoolClass);
        assertThat(result.getTeacher()).isEqualTo(teacher);
        assertThat(result.getStatus())
                .isEqualTo(LessonStatus.SCHEDULED);

        verify(lessonRepository).save(any(Lesson.class));
        verify(schoolClassRepository).findById(schoolClassId);
        verify(subjectRepository).findById(subjectId);
        verify(userService).findTeacherByIdOrThrow(teacherId);

    }

    @Test
    void createLesson_shouldThrow_whenStartTimeAfterEndTime() {
        LocalDateTime startTime = LocalDateTime.of(
                2026, 5, 20, 9, 10, 0);
        LocalDateTime endTime = LocalDateTime.of(
                2026, 5, 20, 8, 30, 0);


        assertThatThrownBy(() ->
            lessonService.createLesson(
                    null,
                    startTime,
                    endTime,
                    null,
                    null,
                    null,
                    null,
                    null
            )).isInstanceOf(InvalidTimesException.class);

        verifyNoInteractions(
                schoolClassRepository,
                subjectRepository,
                termRepository,
                lessonRepository,
                userService
        );
    }

    @Test
    void createLesson_shouldThrow_whenSchoolClassNotFound() {
        Long schoolClassId = 1L;

        when(schoolClassRepository.findById(schoolClassId)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                lessonService.createLesson(
                        null,
                        LESSON_START_TIME,
                        LESSON_END_TIME,
                        null,
                        schoolClassId,
                        null,
                        null,
                        null
                )).isInstanceOf(SchoolClassNotFoundException.class);

        verify(schoolClassRepository).findById(schoolClassId);
        verifyNoInteractions(
                subjectRepository,
                termRepository,
                lessonRepository,
                userService
        );
    }

    @Test
    void createLesson_shouldThrow_whenSchoolClassIsArchived() {
        SchoolClass schoolClass = getSchoolClass();
        schoolClass.setAcademicYear("2024");
        Long schoolClassId = schoolClass.getId();

        when(schoolClassRepository.findById(schoolClassId))
                .thenReturn(Optional.of(schoolClass));

        assertThatThrownBy(() ->
                lessonService.createLesson(
                        null,
                        LESSON_START_TIME,
                        LESSON_END_TIME,
                        null,
                        schoolClassId,
                        null,
                        null,
                        null
                )).isInstanceOf(ClassIsArchivedException.class)
                .hasMessageContaining(schoolClass.getAcademicYear());

        verify(schoolClassRepository).findById(schoolClassId);
        verifyNoInteractions(
                subjectRepository,
                termRepository,
                lessonRepository,
                userService
        );
    }

    @Test
    void createLesson_shouldThrow_whenUserNotFound() {
        SchoolClass schoolClass = getSchoolClass();

        when(schoolClassRepository.findById(1L)).thenReturn(Optional.of(schoolClass));
        when(userService.findTeacherByIdOrThrow(1L)).thenThrow(new UserNotFoundException(1L));

        assertThatThrownBy(() ->
                lessonService.createLesson(
                        null, LESSON_START_TIME, LESSON_END_TIME, null,
                        1L, null, 1L, null
                )).isInstanceOf(UserNotFoundException.class);
        verify(userService).findTeacherByIdOrThrow(1L);
        verifyNoInteractions(
                subjectRepository,
                termRepository,
                lessonRepository
        );
    }

    @Test
    void createLesson_shouldThrow_whenUserNotTeacher() {
        SchoolClass schoolClass = getSchoolClass();

        when(schoolClassRepository.findById(1L)).thenReturn(Optional.of(schoolClass));
        when(userService.findTeacherByIdOrThrow(1L))
                .thenThrow(new NotTeacherException(1L));

        assertThatThrownBy(() ->
                lessonService.createLesson(
                        null, LESSON_START_TIME, LESSON_END_TIME, null,
                        1L, null, 1L, null
                )).isInstanceOf(NotTeacherException.class);
        verify(userService).findTeacherByIdOrThrow(1L);
        verifyNoInteractions(
                subjectRepository,
                termRepository,
                lessonRepository
        );
    }

    @Test
    void createLesson_shouldThrow_whenSubjectNotFound() {
        SchoolClass schoolClass = getSchoolClass();
        Long classId = 1L;
        Long subjectId = 1L;
        User teacher = getMathsTeacher();
        Long teacherId = teacher.getId();

        when(schoolClassRepository.findById(classId)).thenReturn(Optional.of(schoolClass));
        when(userService.findTeacherByIdOrThrow(teacherId))
                .thenReturn(teacher);
        when(subjectRepository.findById(subjectId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                lessonService.createLesson(
                        null, LESSON_START_TIME, LESSON_END_TIME, null,
                        classId, subjectId, teacherId, null
                )).isInstanceOf(SubjectNotFoundException.class);
        verify(subjectRepository).findById(subjectId);
        verify(userService).findTeacherByIdOrThrow(teacherId);
        verifyNoInteractions(termRepository, lessonRepository);
    }

    @Test
    void createLesson_shouldThrow_whenTeacherNotTeachesTheSubject() {
        SchoolClass schoolClass = getSchoolClass();
        Long classId = 1L;

        Subject subject = new Subject("PE");
        subject.setId(2L);
        Long subjectId = subject.getId();

        User teacher = getMathsTeacher();
        Long teacherId = teacher.getId();

        when(schoolClassRepository.findById(classId)).thenReturn(Optional.of(schoolClass));
        when(userService.findTeacherByIdOrThrow(teacherId))
                .thenReturn(teacher);
        when(subjectRepository.findById(subject.getId()))
                .thenReturn(Optional.of(subject));

        assertThatThrownBy(() ->
                lessonService.createLesson(
                        null, LESSON_START_TIME, LESSON_END_TIME, null,
                        classId, subjectId, teacherId, null
                )).isInstanceOf(TeacherDoesNotTeachSubjectException.class);

        verify(subjectRepository).findById(subjectId);
        verify(userService).findTeacherByIdOrThrow(teacherId);
        verifyNoInteractions(termRepository, lessonRepository);
    }

    @Test
    void createLesson_shouldThrow_whenTermNotFound() {
        SchoolClass schoolClass = getSchoolClass();
        Long classId = 1L;

        Subject subject = new Subject("Maths");
        subject.setId(1L);
        Long subjectId = subject.getId();

        User teacher = getMathsTeacher();
        Long teacherId = teacher.getId();

        Long termId = 1L;

        when(schoolClassRepository.findById(classId)).thenReturn(Optional.of(schoolClass));
        when(userService.findTeacherByIdOrThrow(teacherId))
                .thenReturn(teacher);
        when(subjectRepository.findById(subjectId))
                .thenReturn(Optional.of(subject));
        when(termRepository.findByIdAndAcademicYear(termId, currentAcademicYear))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                lessonService.createLesson(
                        null, LESSON_START_TIME, LESSON_END_TIME, null,
                        classId, subjectId, teacherId, termId
                )).isInstanceOf(TermNotFoundException.class);

        verify(subjectRepository).findById(subjectId);
        verify(userService).findTeacherByIdOrThrow(teacherId);
        verify(termRepository).findByIdAndAcademicYear(termId, currentAcademicYear);
        verifyNoInteractions(lessonRepository);
    }

    @Test
    void createLesson_shouldThrow_whenLessonOutOfTerm() {
        LocalDateTime lessonStartTime = LocalDateTime.of(
                2026, 2, 20, 8, 30, 0);
        LocalDateTime lessonEndTime = LocalDateTime.of(
                2026, 2, 20, 9, 10, 0);

        SchoolClass schoolClass = getSchoolClass();
        Long classId = 1L;

        Subject subject = new Subject("Maths");
        subject.setId(1L);
        Long subjectId = subject.getId();

        User teacher = getMathsTeacher();
        Long teacherId = teacher.getId();

        Term term = getTermOfCurrentYear();
        term.setId(1L);
        Long termId = term.getId();

        when(schoolClassRepository.findById(classId)).thenReturn(Optional.of(schoolClass));
        when(userService.findTeacherByIdOrThrow(teacherId))
                .thenReturn(teacher);
        when(subjectRepository.findById(subjectId))
                .thenReturn(Optional.of(subject));
        when(termRepository.findByIdAndAcademicYear(termId, currentAcademicYear))
                .thenReturn(Optional.of(term));

        assertThatThrownBy(() ->
                lessonService.createLesson(
                        null, lessonStartTime, lessonEndTime, null,
                        classId, subjectId, teacherId, termId
                )).isInstanceOf(LessonOutOfTermException.class);

        verify(subjectRepository).findById(subjectId);
        verify(userService).findTeacherByIdOrThrow(teacherId);
        verify(termRepository).findByIdAndAcademicYear(termId, currentAcademicYear);
        verifyNoInteractions(lessonRepository);
    }

    @Test
    void createLesson_shouldThrow_whenClassHasOverlappingLesson() {
        SchoolClass schoolClass = getSchoolClass();
        Long classId = 1L;

        Subject subject = new Subject("Maths");
        subject.setId(1L);
        Long subjectId = subject.getId();

        User teacher = getMathsTeacher();
        Long teacherId = teacher.getId();

        Term term = getTermOfCurrentYear();
        term.setId(1L);
        Long termId = term.getId();

        when(schoolClassRepository.findById(classId)).thenReturn(Optional.of(schoolClass));
        when(userService.findTeacherByIdOrThrow(teacherId))
                .thenReturn(teacher);
        when(subjectRepository.findById(subjectId))
                .thenReturn(Optional.of(subject));
        when(termRepository.findByIdAndAcademicYear(termId, currentAcademicYear))
                .thenReturn(Optional.of(term));
        when(lessonRepository.existsOverlappingTimes(classId, LESSON_START_TIME, LESSON_END_TIME))
                .thenReturn(true);

        assertThatThrownBy(() ->
                lessonService.createLesson(
                        null, LESSON_START_TIME, LESSON_END_TIME, null,
                        classId, subjectId, teacherId, termId
                )).isInstanceOf(LessonConflictException.class)
                .hasMessageContaining(LessonConflictType.CLASS_OCCUPIED.getMessage());

        verify(subjectRepository).findById(subjectId);
        verify(userService).findTeacherByIdOrThrow(teacherId);
        verify(termRepository).findByIdAndAcademicYear(termId, currentAcademicYear);
        verify(lessonRepository)
                .existsOverlappingTimes(classId, LESSON_START_TIME, LESSON_END_TIME);
    }

    @Test
    void createLesson_shouldThrow_whenTeacherHasOverlappingLesson() {
        SchoolClass schoolClass = getSchoolClass();
        Long classId = 1L;

        Subject subject = new Subject("Maths");
        subject.setId(1L);
        Long subjectId = subject.getId();

        User teacher = getMathsTeacher();
        Long teacherId = teacher.getId();

        Term term = getTermOfCurrentYear();
        term.setId(1L);
        Long termId = term.getId();

        when(schoolClassRepository.findById(classId)).thenReturn(Optional.of(schoolClass));
        when(userService.findTeacherByIdOrThrow(teacherId))
                .thenReturn(teacher);
        when(subjectRepository.findById(subjectId))
                .thenReturn(Optional.of(subject));
        when(termRepository.findByIdAndAcademicYear(termId, currentAcademicYear))
                .thenReturn(Optional.of(term));
        when(lessonRepository.existsOverlappingTimes(classId, LESSON_START_TIME, LESSON_END_TIME))
                .thenReturn(false);
        when(lessonRepository.existsOverlappingByTeacher(teacherId, LESSON_START_TIME, LESSON_END_TIME))
                .thenReturn(true);

        assertThatThrownBy(() ->
                lessonService.createLesson(
                        null, LESSON_START_TIME, LESSON_END_TIME, null,
                        classId, subjectId, teacherId, termId
                )).isInstanceOf(LessonConflictException.class)
                .hasMessageContaining(LessonConflictType.TEACHER_BUSY.getMessage());

        verify(subjectRepository).findById(subjectId);
        verify(userService).findTeacherByIdOrThrow(teacherId);
        verify(termRepository).findByIdAndAcademicYear(termId, currentAcademicYear);
        verify(lessonRepository)
                .existsOverlappingTimes(classId, LESSON_START_TIME, LESSON_END_TIME);
        verify(lessonRepository)
                .existsOverlappingByTeacher(teacherId, LESSON_START_TIME, LESSON_END_TIME);
    }


    @Test
    void updateLesson_shouldUpdateLesson_whenStatusIsScheduled() {
        Long lessonId = 1L;
        String topic = "Многочлены";
        LocalDateTime lessonStartTime =  LocalDateTime
                .of(2026, 5, 20, 9, 20, 0);
        LocalDateTime lessonEndTime = LocalDateTime
                .of(2026, 5, 20, 10, 0, 0);
        String room = "42";
        Long schoolClassId = 1L;
        Long subjectId = 1L;
        Long teacherId = 1L;
        Long  termId = 1L;

        Lesson lesson = getLesson();
        SchoolClass schoolClass = getSchoolClass();
        User mathsTeacher  = getMathsTeacher();

        Subject subject = new Subject("Maths");
        subject.setId(1L);

        Term term = getTermOfCurrentYear();

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(schoolClassRepository.findById(schoolClassId))
                .thenReturn(Optional.of(schoolClass));
        when(userService.findTeacherByIdOrThrow(teacherId)).thenReturn(mathsTeacher);
        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));
        when(termRepository.findByIdAndAcademicYear(termId, currentAcademicYear))
                .thenReturn(Optional.of(term));
        when(lessonRepository.existsOverlappingTimesExcludingId(
                lessonId, schoolClassId, lessonStartTime, lessonEndTime)).thenReturn(false);
        when(lessonRepository.existsOverlappingByTeacherExcludingId(
                lessonId, teacherId, lessonStartTime, lessonEndTime)).thenReturn(false);

        lessonService.updateLesson(
                lessonId,
                topic,
                lessonStartTime,
                lessonEndTime,
                room,
                schoolClassId,
                subjectId,
                teacherId,
                termId
        );

        assertThat(lesson.getTopic()).isEqualTo(topic);
        assertThat(lesson.getStartTime()).isEqualTo(lessonStartTime);
        assertThat(lesson.getEndTime()).isEqualTo(lessonEndTime);
        assertThat(lesson.getRoom()).isEqualTo(room);
        assertThat(lesson.getSchoolClass()).isEqualTo(schoolClass);
        assertThat(lesson.getTeacher()).isEqualTo(mathsTeacher);
        assertThat(lesson.getSubject()).isEqualTo(subject);
        assertThat(lesson.getTerm()).isEqualTo(term);

        verify(lessonRepository)
                .existsOverlappingTimesExcludingId(
                        lessonId, schoolClassId, lessonStartTime, lessonEndTime);

        verify(lessonRepository)
                .existsOverlappingByTeacherExcludingId(
                        lessonId, teacherId, lessonStartTime, lessonEndTime);
        verify(lessonRepository, never()).save(any());
    }

    @Test
    void updateLesson_shouldThrow_whenLessonNotFound() {
        Long lessonId = 1L;

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> lessonService.updateLesson(
                lessonId,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        )).isInstanceOf(LessonNotFoundException.class);
        verifyNoInteractions(
                schoolClassRepository,
                subjectRepository,
                termRepository,
                userService
        );
    }

    @Test
    void updateLesson_shouldThrow_whenLessonStatusNotScheduled() {
        Lesson lesson = getLesson();
        Long lessonId = lesson.getId();
        lesson.setStatus(LessonStatus.COMPLETED);

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));

        assertThatThrownBy(() -> lessonService.updateLesson(
                lessonId,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        )).isInstanceOf(LessonNotEditableException.class);
        verify(lessonRepository).findById(lessonId);
        verifyNoMoreInteractions(lessonRepository);
    }

    @Test
    void updateLesson_shouldThrow_whenEndTimeIsBeforeStartTime() {
        Lesson lesson = getLesson();
        Long lessonId = lesson.getId();
        LocalDateTime startTime = LocalDateTime.of(
                2026, 5, 20, 9, 10, 0);
        LocalDateTime endTime = LocalDateTime.of(
                2026, 5, 20, 8, 30, 0);

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));

        assertThatThrownBy(() -> lessonService.updateLesson(
                lessonId,
                null,
                startTime,
                endTime,
                null,
                null,
                null,
                null,
                null
        )).isInstanceOf(InvalidTimesException.class);
        verify(lessonRepository).findById(lessonId);
        verifyNoMoreInteractions(lessonRepository);
        verifyNoInteractions(
                schoolClassRepository,
                subjectRepository,
                termRepository,
                userService
        );
    }

    @Test
    void updateLessonContent_shouldUpdateLesson_whenTeacherIsCurrentUser() {
        Lesson lesson = getLesson();
        Long lessonId = lesson.getId();
        Long currentUserId = 1L;
        String newTopic = "New Topic";
        String newRoom = "New Room";

        when(lessonRepository.findById(lesson.getId())).thenReturn(Optional.of(lesson));

        lessonService.updateLessonContent(lessonId, currentUserId, newTopic, newRoom);

        assertThat(lesson.getTopic()).isEqualTo(newTopic);
        assertThat(lesson.getRoom()).isEqualTo(newRoom);
        verify(lessonRepository, never()).save(any());
    }

    @Test
    void updateLessonContent_shouldThrow_whenTeacherIsNotCurrentUser() {
        Lesson lesson = getLesson();
        Long lessonId = lesson.getId();
        Long currentUserId = 99L;

        when(lessonRepository.findById(lesson.getId())).thenReturn(Optional.of(lesson));

        assertThatThrownBy(
                () -> lessonService.updateLessonContent(
                        lessonId, currentUserId, null, null)
        ).isInstanceOf(AccessDeniedException.class);
        verify(lessonRepository).findById(lessonId);
        verifyNoMoreInteractions(lessonRepository);
        verify(lessonRepository, never()).save(any());
    }

    @Test
    void completeLesson_shouldChangeLessonStatusAsCompleted() {
        Lesson lesson = getLesson();
        Long lessonId = lesson.getId();
        Long currentUserId = 1L;

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        lessonService.completeLesson(lessonId, currentUserId);

        assertThat(lesson.getStatus()).isEqualTo(LessonStatus.COMPLETED);
        verify(lessonRepository, never()).save(any());
    }

    @Test
    void cancelLesson_shouldChangeLessonStatusAsCancelled() {
        Lesson lesson = getLesson();
        Long lessonId = lesson.getId();
        Long currentUserId = 1L;

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        lessonService.cancelLesson(lessonId, currentUserId);

        assertThat(lesson.getStatus()).isEqualTo(LessonStatus.CANCELLED);
        verify(lessonRepository, never()).save(any());
    }

    @Test
    void deleteLesson_shouldDeleteLesson_whenLessonStatusIsScheduled() {
        Lesson lesson = getLesson();
        Long lessonId = lesson.getId();

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        lessonService.deleteLesson(lessonId);

        verify(lessonRepository).findById(lessonId);
        verify(lessonRepository).delete(lesson);
        verifyNoMoreInteractions(lessonRepository);
    }

    private Lesson getLesson() {
        Subject subject = new Subject("Maths");
        subject.setId(1L);

        return Lesson.builder()
                .id(1L)
                .topic("Кому на Руси жить хорошо?")
                .startTime(LESSON_START_TIME)
                .endTime(LESSON_END_TIME)
                .room("666")
                .schoolClass(getSchoolClass())
                .subject(subject)
                .teacher(getMathsTeacher())
                .term(getTermOfCurrentYear())
                .build();
    }

    private SchoolClass getSchoolClass() {
        SchoolClass schoolClass = new SchoolClass("5", currentAcademicYear);
        schoolClass.setId(1L);
        return schoolClass;
    }

    private User getMathsTeacher() {
        Subject maths = new Subject("Maths");
        maths.setId(1L);

        return User.builder()
                .id(1L)
                .email("teacher@teacher.com")
                .password("password")
                .firstName("Михаил")
                .lastName("Иванов")
                .subjects(Set.of(maths))
                .createdAt(Instant.now())
                .roles(Set.of(new Role(1L, RoleName.TEACHER)))
                .build();

    }

    private Term getTermOfCurrentYear() {
        return new Term(
                "4 четверть", LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 5, 30),
                currentAcademicYear);
    }
}
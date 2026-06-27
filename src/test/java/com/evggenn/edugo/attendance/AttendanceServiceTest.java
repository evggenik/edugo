package com.evggenn.edugo.attendance;

import com.evggenn.edugo.attendance.exception.AttendanceAlreadyExistsException;
import com.evggenn.edugo.attendance.exception.LessonNotCompletedException;
import com.evggenn.edugo.lesson.Lesson;
import com.evggenn.edugo.lesson.LessonRepository;
import com.evggenn.edugo.lesson.LessonStatus;
import com.evggenn.edugo.lesson.exception.LessonNotFoundException;
import com.evggenn.edugo.user.User;
import com.evggenn.edugo.user.UserService;
import com.evggenn.edugo.user.exception.UserNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private AttendanceService attendanceService;

    @Test
    void createAttendance_shouldReturnAttendance_whenLessonCompleted() {
        AttendanceStatus status = AttendanceStatus.ILL;
        Long studentId = 1L;
        Long lessonId = 10L;
        Long currentUserId = 100L;

        User student = User.builder().id(studentId).build();
        User teacher = User.builder().id(100L).build();
        Lesson lesson =  Lesson.builder()
                .id(lessonId)
                .teacher(teacher)
                .status(LessonStatus.COMPLETED)
                .build();

        when(userService.findStudentByIdOrThrow(studentId)).thenReturn(student);
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(attendanceRepository.existsByStudentIdAndLessonId(studentId, lessonId))
                .thenReturn(false);
        when(attendanceRepository.save(any(Attendance.class)))
                .thenAnswer(i -> i.getArguments()[0]);

        Attendance result = attendanceService.createAttendance(
                status, studentId, lessonId, currentUserId);

        assertThat(result.getStudent().getId()).isEqualTo(studentId);
        assertThat(result.getLesson().getId()).isEqualTo(lessonId);
        assertThat(result.getStatus()).isEqualTo(status);

        verify(attendanceRepository).save(any(Attendance.class));
    }

    @Test
    void createAttendance_shouldThrow_whenStudentNotFound() {
        AttendanceStatus status = AttendanceStatus.ILL;
        Long studentId = 1L;
        Long lessonId = 10L;
        Long currentUserId = 100L;

        when(userService.findStudentByIdOrThrow(studentId))
                .thenThrow(new UserNotFoundException(studentId));

        assertThatThrownBy(() -> attendanceService.createAttendance(
                status, studentId, lessonId, currentUserId
        )).isInstanceOf(UserNotFoundException.class);

        verify(userService)
                .findStudentByIdOrThrow(studentId);

        verifyNoInteractions(
                lessonRepository,
                attendanceRepository
        );
    }

    @Test
    void createAttendance_shouldThrow_whenLessonNotFound() {
        AttendanceStatus status = AttendanceStatus.ILL;
        Long studentId = 1L;
        Long lessonId = 10L;
        Long currentUserId = 100L;

        User student = User.builder().id(studentId).build();

        when(userService.findStudentByIdOrThrow(studentId))
                .thenReturn(student);

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> attendanceService.createAttendance(
                status, studentId, lessonId, currentUserId
        )).isInstanceOf(LessonNotFoundException.class);

        verify(userService)
                .findStudentByIdOrThrow(studentId);
        verify(lessonRepository)
                .findById(lessonId);

        verifyNoInteractions(attendanceRepository);
    }

    @Test
    void createAttendance_shouldThrow_whenCurrentUserIsNotLessonTeacher() {
        AttendanceStatus status = AttendanceStatus.ILL;
        Long studentId = 1L;
        Long lessonId = 10L;
        Long currentUserId = 100L;

        User student = User.builder().id(studentId).build();
        User teacher = User.builder().id(999L).build();
        Lesson lesson =  Lesson.builder()
                .id(lessonId)
                .teacher(teacher)
                .status(LessonStatus.COMPLETED)
                .build();

        when(userService.findStudentByIdOrThrow(studentId))
                .thenReturn(student);
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));

        assertThatThrownBy(() -> attendanceService.createAttendance(
                status, studentId, lessonId, currentUserId
        )).isInstanceOf(AccessDeniedException.class);

        verify(userService)
                .findStudentByIdOrThrow(studentId);
        verify(lessonRepository)
                .findById(lessonId);

        verifyNoInteractions(attendanceRepository);
    }

    @Test
    void createAttendance_shouldThrow_whenLessonStatusIsNotCompleted() {
        AttendanceStatus status = AttendanceStatus.ILL;
        Long studentId = 1L;
        Long lessonId = 10L;
        Long currentUserId = 100L;

        User student = User.builder().id(studentId).build();
        User teacher = User.builder().id(100L).build();
        Lesson lesson =  Lesson.builder()
                .id(lessonId)
                .teacher(teacher)
                .status(LessonStatus.SCHEDULED)
                .build();

        when(userService.findStudentByIdOrThrow(studentId))
                .thenReturn(student);
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));

        assertThatThrownBy(() -> attendanceService.createAttendance(
                status, studentId, lessonId, currentUserId
        )).isInstanceOf(LessonNotCompletedException.class);

        verify(userService)
                .findStudentByIdOrThrow(studentId);
        verify(lessonRepository)
                .findById(lessonId);

        verifyNoInteractions(attendanceRepository);
    }

    @Test
    void createAttendance_shouldThrow_whenAttendanceAlreadyExists() {
        AttendanceStatus status = AttendanceStatus.ILL;
        Long studentId = 1L;
        Long lessonId = 10L;
        Long currentUserId = 100L;

        User student = User.builder().id(studentId).build();
        User teacher = User.builder().id(100L).build();
        Lesson lesson =  Lesson.builder()
                .id(lessonId)
                .teacher(teacher)
                .status(LessonStatus.COMPLETED)
                .build();

        when(userService.findStudentByIdOrThrow(studentId))
                .thenReturn(student);
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(attendanceRepository.existsByStudentIdAndLessonId(studentId, lessonId))
                .thenReturn(true);

        assertThatThrownBy(() -> attendanceService.createAttendance(
                status, studentId, lessonId, currentUserId
        )).isInstanceOf(AttendanceAlreadyExistsException.class);

        verify(userService)
                .findStudentByIdOrThrow(studentId);
        verify(lessonRepository)
                .findById(lessonId);
        verify(attendanceRepository)
                .existsByStudentIdAndLessonId(studentId, lessonId);

        verifyNoMoreInteractions(attendanceRepository);
    }

    @Test
    void getAttendanceByLesson_shouldReturnListOfAttendance() {
        Long lessonId = 1L;
        Lesson lesson = Lesson.builder().id(lessonId).build();

        User student1 =  User.builder().id(10L).build();
        User student2 =  User.builder().id(20L).build();

        Attendance attendance1 = Attendance.builder()
                .status(AttendanceStatus.ABSENT).lesson(lesson).student(student1).build();
        Attendance attendance2 = Attendance.builder()
                .status(AttendanceStatus.ILL).lesson(lesson).student(student2).build();

        List<Attendance> attendances = List.of(attendance1, attendance2);

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(attendanceRepository.findAllByLessonId(lessonId))
                .thenReturn(attendances);

        List<Attendance> result = attendanceService.getAttendanceByLesson(lessonId);

        assertThat(result).isEqualTo(attendances);

        verify(lessonRepository).findById(lessonId);
        verify(attendanceRepository).findAllByLessonId(lessonId);
    }

    @Test
    void getAttendanceByLesson_shouldThrow_whenLessonNotFound() {
        Long lessonId = 1L;

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> attendanceService.getAttendanceByLesson(lessonId))
                .isInstanceOf(LessonNotFoundException.class);

        verify(lessonRepository).findById(lessonId);
        verifyNoInteractions(attendanceRepository);
    }
}
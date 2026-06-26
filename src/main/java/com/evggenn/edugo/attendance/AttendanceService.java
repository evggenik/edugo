package com.evggenn.edugo.attendance;

import com.evggenn.edugo.attendance.exception.AttendanceAlreadyExistsException;
import com.evggenn.edugo.attendance.exception.LessonNotCompletedException;
import com.evggenn.edugo.lesson.Lesson;
import com.evggenn.edugo.lesson.LessonRepository;
import com.evggenn.edugo.lesson.LessonStatus;
import com.evggenn.edugo.lesson.exception.LessonNotFoundException;
import com.evggenn.edugo.user.User;
import com.evggenn.edugo.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final LessonRepository lessonRepository;
    private final UserService  userService;

    @Transactional
    public Attendance createAttendance(
            AttendanceStatus status,
            Long studentId,
            Long lessonId,
            Long currentUserId) {

        User student = userService.findStudentByIdOrThrow(studentId);

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new LessonNotFoundException(lessonId));

        if (!lesson.getTeacher().getId().equals(currentUserId)) {
            throw new AccessDeniedException(
                    "You can only mark attendance for your own lessons");
        }

        if (lesson.getStatus() != LessonStatus.COMPLETED) {
            throw new LessonNotCompletedException(lesson.getStatus());
        }

        if (attendanceRepository.existsByStudentIdAndLessonId(studentId, lessonId)) {
            throw new AttendanceAlreadyExistsException(studentId, lessonId);
        }

        Attendance attendance = Attendance.builder()
                .status(status)
                .student(student)
                .lesson(lesson)
                .build();

        return attendanceRepository.save(attendance);
    }

    @Transactional(readOnly = true)
    public List<Attendance> getAttendanceByLesson(Long lessonId) {

        lessonRepository.findById(lessonId).orElseThrow(
                () -> new LessonNotFoundException(lessonId)
        );

        return attendanceRepository.findAllByLessonId(lessonId);
    }
}

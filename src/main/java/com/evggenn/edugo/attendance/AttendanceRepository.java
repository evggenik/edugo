package com.evggenn.edugo.attendance;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    boolean existsByStudentIdAndLessonId(Long studentId, Long lessonId);

    List<Attendance> findAllByLessonId(Long lessonId);
}

package com.evggenn.edugo.attendance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    boolean existsByStudentIdAndLessonId(Long studentId, Long lessonId);

    @Query("""
        SELECT a FROM Attendance a
        JOIN FETCH a.student
        WHERE a.lesson.id = :lessonId
           """)
    List<Attendance> findAllByLessonId(Long lessonId);
}

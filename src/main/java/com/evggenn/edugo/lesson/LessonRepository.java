package com.evggenn.edugo.lesson;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface LessonRepository extends JpaRepository<Lesson, Long> {

    @Query("""
        SELECT COUNT(l) > 0 FROM Lesson l
        WHERE l.schoolClass.id = :classId
        AND l.startTime <= :endTime
        AND l.endTime >= :startTime
    """)
    boolean existsOverlappingTimes(Long classId,
                                   LocalDateTime startTime,
                                   LocalDateTime endTime);

    @Query("""
        SELECT COUNT(l) > 0 FROM Lesson l
        WHERE l.id <> :id
        AND l.schoolClass.id = :classId
        AND l.startTime <= :endTime
        AND l.endTime >= :startTime
    """)
    boolean existsOverlappingTimesExcludingId(Long id,
                                              Long classId,
                                              LocalDateTime startTime,
                                              LocalDateTime endTime);

    @Query("""
    SELECT COUNT(l) > 0 FROM Lesson l
    WHERE l.teacher.id = :teacherId
    AND l.startTime <= :endTime
    AND l.endTime >= :startTime
    """)
    boolean existsOverlappingByTeacher(Long teacherId,
                                       LocalDateTime startTime,
                                       LocalDateTime endTime);

    @Query("""
    SELECT COUNT(l) > 0 FROM Lesson l
    WHERE l.id <> :id
    AND l.teacher.id = :teacherId
    AND l.startTime <= :endTime
    AND l.endTime >= :startTime
    """)
    boolean existsOverlappingByTeacherExcludingId(Long id, Long teacherId,
                                                  LocalDateTime startTime,
                                                  LocalDateTime endTime);

    @Query("""
        SELECT l FROM Lesson l
        JOIN FETCH l.subject
        JOIN FETCH l.teacher
        JOIN FETCH l.term
        JOIN FETCH l.schoolClass
        WHERE l.id = :lessonId
       """)
    Optional<Lesson> findByIdWithDetails(Long lessonId);

    @Query("""
        SELECT l FROM Lesson l
        JOIN FETCH l.subject
        JOIN FETCH l.teacher
        JOIN FETCH l.term
        JOIN FETCH l.schoolClass
        WHERE l.schoolClass.id = :classId
        AND l.term.id = :termId
       """)
    List<Lesson> findAllBySchoolClassIdAndTermId(Long classId, Long termId);

    @Query("""
        SELECT l FROM Lesson l
        JOIN FETCH l.subject
        JOIN FETCH l.teacher
        JOIN FETCH l.term
        JOIN FETCH l.schoolClass
        WHERE l.teacher.id = :teacherId
        AND l.term.id = :termId
       """)
    List<Lesson> findAllByTeacherIdAndTermId(Long teacherId, Long termId);

    @Query("""
        SELECT l FROM Lesson l
        JOIN FETCH l.subject
        JOIN FETCH l.teacher
        JOIN FETCH l.term
        JOIN FETCH l.schoolClass
        WHERE l.schoolClass.id = :classId
        AND l.startTime BETWEEN :from AND :to
       """)
    List<Lesson> findAllBySchoolClassIdAndStartTimeBetween(
            Long classId, LocalDateTime from, LocalDateTime to);

    @Query("""
        SELECT l FROM Lesson l
        JOIN FETCH l.subject
        JOIN FETCH l.teacher
        JOIN FETCH l.term
        JOIN FETCH l.schoolClass
        WHERE l.teacher.id = :teacherId
        AND l.startTime BETWEEN :from AND :to
       """)
    List<Lesson> findAllByTeacherIdAndStartTimeBetween(
            Long teacherId, LocalDateTime from, LocalDateTime to);
}

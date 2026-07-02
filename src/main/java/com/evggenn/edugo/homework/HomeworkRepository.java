package com.evggenn.edugo.homework;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface HomeworkRepository extends JpaRepository<Homework, Long> {
    boolean existsByLessonId(Long lessonId);

    Optional<Homework> findByLessonId(Long lessonId);

    @Query("SELECT h FROM Homework h JOIN FETCH h.lesson WHERE h.id = :id")
    Optional<Homework> findByIdWithLesson(Long id);

    @Query("""
            SELECT h FROM Homework h
            JOIN FETCH h.lesson l
            JOIN FETCH l.teacher
            WHERE h.id = :id
            """)
    Optional<Homework> findByIdWithDetails(Long id);
}

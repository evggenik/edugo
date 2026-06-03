package com.evggenn.edugo.grade;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface GradeRepository extends JpaRepository<Grade, Long> {

    @Query("""
        SELECT g FROM Grade g
        JOIN FETCH g.student
        JOIN FETCH g.subject
        LEFT JOIN FETCH g.lesson
        LEFT JOIN FETCH g.term
        WHERE g.id = :id
        """)
    Optional<Grade> findByIdWithDetails(Long id);

    @Query("""
        SELECT g FROM Grade g
        JOIN FETCH g.student
        JOIN FETCH g.subject
        LEFT JOIN FETCH g.lesson
        LEFT JOIN FETCH g.term
        WHERE g.student.id = :studentId
        AND g.subject.id = :subjectId
        AND g.term.id = :termId
        """)
    List<Grade> findAllWithDetailsBySubjectAndTermAndStudent(
            Long subjectId, Long termId, Long studentId);
}

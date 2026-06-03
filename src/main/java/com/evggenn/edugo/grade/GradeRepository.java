package com.evggenn.edugo.grade;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface GradeRepository extends JpaRepository<Grade, Long> {

    @Query("""
        SELECT g FROM Grade g
        WHERE g.subject.id = : subjectId
        AND g.term.id = : termId
        AND g.student.id = : studentId
    """)
    List<Grade> findAllBySubjectAndTermAndStudent(
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

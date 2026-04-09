package com.evggenn.edugo.schoolclass;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SchoolClassRepository extends JpaRepository<SchoolClass, Long> {

    boolean existsByNameAndAcademicYear(String name, String academicYear);

    @Query("""
    SELECT CASE WHEN COUNT(sc) > 0 THEN true ELSE false END
    FROM SchoolClass sc
    JOIN sc.students s
    WHERE s.id = :studentId
      AND sc.academicYear = :academicYear
    """)
    boolean existsByStudentIdAndAcademicYear(Long studentId, String academicYear);

    @Query("""
    SELECT DISTINCT sc
    FROM SchoolClass sc
    LEFT JOIN FETCH sc.teacher
    LEFT JOIN FETCH sc.students
    WHERE sc.id =:id
    """)
    Optional<SchoolClass> findByIdWithDetails(@Param("id") Long id);

    @Query("""
    SELECT DISTINCT sc
    FROM SchoolClass sc
    LEFT JOIN FETCH sc.teacher
    WHERE sc.academicYear = :academicYear
    """)
    List<SchoolClass> findAllByYearWithTeacher(@Param("academicYear") String academicYear);
}

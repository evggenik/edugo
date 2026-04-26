package com.evggenn.edugo.term;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TermRepository extends JpaRepository<Term, Long> {

    boolean existsByNameAndAcademicYear(String name, String academicYear);

    Optional<Term> findByIdAndAcademicYear(Long id, String academicYear);

    List<Term> findAllByAcademicYear(String academicYear);

    @Query("""
    SELECT COUNT(p) > 0 FROM Term p
    WHERE p.academicYear = :academicYear
    AND p.startDate <= :newEnd
    AND p.endDate >= :newStart
    AND p.id <> :id
""")
    boolean existsOverlappingPeriodExcludingId(
            String academicYear,
            LocalDate newStart,
            LocalDate newEnd,
            Long id
    );

    @Query("""
        SELECT COUNT(p) > 0 FROM Term p
        WHERE p.academicYear = :academicYear
        AND p.startDate <= :newEnd
        AND p.endDate >= :newStart
    """)
    boolean existsOverlappingPeriod(
            String academicYear,
            LocalDate newStart,
            LocalDate newEnd
    );
}

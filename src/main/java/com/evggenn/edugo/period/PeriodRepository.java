package com.evggenn.edugo.period;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface PeriodRepository extends JpaRepository<Period, Long> {

    boolean existsByNameAndAcademicYear(String name, String academicYear);

    List<Period> findAllByAcademicYear(String academicYear);

    @Query("""
        SELECT COUNT(p) > 0 FROM Period p
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

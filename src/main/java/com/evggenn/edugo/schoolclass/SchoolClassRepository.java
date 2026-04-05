package com.evggenn.edugo.schoolclass;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SchoolClassRepository extends JpaRepository<SchoolClass, Long> {

    Optional<SchoolClass> findByNameAndAcademicYear(String name, String academicYear);

    boolean existsByNameAndAcademicYear(String name, String academicYear);

    List<SchoolClass> findAllByAcademicYear(String academicYear);
}

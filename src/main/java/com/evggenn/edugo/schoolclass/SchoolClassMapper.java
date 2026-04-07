package com.evggenn.edugo.schoolclass;

import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class SchoolClassMapper {

    public SchoolClassResponse toResponse(SchoolClass schoolClass) {
        TeacherResponse teacherResponse = schoolClass.getTeacher() != null
                ? new TeacherResponse(
                schoolClass.getTeacher().getId(),
                schoolClass.getTeacher().getFirstName(),
                schoolClass.getTeacher().getLastName())
                : null;

        Set<StudentResponse> students = schoolClass.getStudents().stream()
                .map(s -> new StudentResponse(s.getId(), s.getFirstName(), s.getLastName()))
                .collect(Collectors.toSet());

        return new SchoolClassResponse(
                schoolClass.getId(),
                schoolClass.getName(),
                schoolClass.getAcademicYear(),
                teacherResponse,
                students
        );
    }
}

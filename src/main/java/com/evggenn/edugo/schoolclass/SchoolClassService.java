package com.evggenn.edugo.schoolclass;

import com.evggenn.edugo.exception.*;
import com.evggenn.edugo.user.Role;
import com.evggenn.edugo.user.User;
import com.evggenn.edugo.user.UserRepository;
import com.evggenn.edugo.util.AcademicYearUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SchoolClassService {

    private final SchoolClassRepository schoolClassRepository;
    private final UserRepository userRepository;
    private final SchoolClassMapper mapper;

    @Transactional
    public SchoolClassResponse createClass(SchoolClassCreateRequest request) {

        String name = request.name();

        if (schoolClassRepository.existsByNameAndAcademicYear(
                name, AcademicYearUtil.getCurrentAcademicYear())) {
            throw new SchoolClassAlreadyExistsException(name);
        }

        SchoolClass schoolClass = new SchoolClass(name, AcademicYearUtil.getCurrentAcademicYear());

        SchoolClass saved = schoolClassRepository.save(schoolClass);

        return mapper.toResponse(saved);
    }

    @Transactional
    public SchoolClassResponse updateClass(Long id, SchoolClassUpdateRequest request) {

        SchoolClass updatedClass = getClassOrThrow(id);

        if (!updatedClass.getAcademicYear().equals(AcademicYearUtil.getCurrentAcademicYear())) {
                 throw new ClassIsArchivedException(updatedClass.getAcademicYear());
        }

        String newName = request.name();
        Long teacherId = request.teacherId();

        if (newName != null &&
                schoolClassRepository.existsByNameAndAcademicYear(
                        newName, AcademicYearUtil.getCurrentAcademicYear()) &&
                !updatedClass.getName().equalsIgnoreCase(newName)) {

            throw new SchoolClassAlreadyExistsException(newName);
        }

        if (teacherId != null) {
            User updatedTeacher = userRepository.findById(teacherId).orElseThrow(
                    () -> new UserNotFoundException(teacherId));
            updatedClass.setTeacher(updatedTeacher);
        }
        if (newName != null) {
            updatedClass.setName(newName);
        }

        return mapper.toResponse(updatedClass);
    }

    @Transactional
    public SchoolClassResponse addStudentToClass(Long classId, Long studentId) {

        SchoolClass schoolClass = getClassOrThrow(classId);

        User student = userRepository.findByIdWithRoles(studentId).orElseThrow(
                () -> new UserNotFoundException(studentId));

        if (student.getRoles().stream()
                .noneMatch(role -> role.getName().equals(Role.STUDENT))) {
            throw new NotStudentException(studentId);
        }

        if (schoolClassRepository.existsByStudentIdAndAcademicYear(
                studentId,
                schoolClass.getAcademicYear())) {
            throw new StudentAlreadyInClassException(studentId, schoolClass.getAcademicYear());
        }
        schoolClass.getStudents().add(student);

        return mapper.toResponse(schoolClass);
    }

    @Transactional
    public SchoolClassResponse removeStudentFromClass(Long classId, Long studentId) {

        SchoolClass schoolClass = getClassOrThrow(classId);

        User student = userRepository.findById(studentId).orElseThrow(
                () -> new UserNotFoundException(studentId));

        schoolClass.getStudents().remove(student);
        return mapper.toResponse(schoolClass);
    }

    @Transactional(readOnly = true)
    public SchoolClassResponse getSchoolClass(Long classId) {

        SchoolClass schoolClass = getClassOrThrow(classId);

        return mapper.toResponse(schoolClass);
    }

    @Transactional(readOnly = true)
    public List<SchoolClassResponse> getAllClasses(String academicYear) {
        List<SchoolClass> classes = schoolClassRepository.findAllByYearWithTeacher(academicYear);
        return classes.stream()
                         .map(mapper::toResponse)
                         .toList();
    }

    private SchoolClass getClassOrThrow(Long classId) {
        return schoolClassRepository.findByIdWithDetails(classId).orElseThrow(
                () -> new SchoolClassNotFoundException(classId));
    }
}

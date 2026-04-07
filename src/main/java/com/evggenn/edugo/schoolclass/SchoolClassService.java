package com.evggenn.edugo.schoolclass;

import com.evggenn.edugo.exception.*;
import com.evggenn.edugo.user.Role;
import com.evggenn.edugo.user.User;
import com.evggenn.edugo.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SchoolClassService {

    private final SchoolClassRepository schoolClassRepository;
    private final UserRepository userRepository;

    @Transactional
    public SchoolClass createClass(String name, String academicYear) {

        if (schoolClassRepository.existsByNameAndAcademicYear(name, academicYear)) {
            throw new SchoolClassAlreadyExistsException(name, academicYear);
        }

        SchoolClass schoolClass = new SchoolClass(name, academicYear);

        return schoolClassRepository.save(schoolClass);
    }

    @Transactional
    public SchoolClass updateClass(Long id,
                                     String newName,
                                     String newYear,
                                     Long teacherId) {

        SchoolClass updatedClass = getClassOrThrow(id);

        if (schoolClassRepository.existsByNameAndAcademicYear(newName, newYear) &&
                !(updatedClass.getName().equalsIgnoreCase(newName) &&
                        updatedClass.getAcademicYear().equals(newYear))) {
            throw new SchoolClassAlreadyExistsException(newName, newYear);
        }

        if (teacherId != null) {
            User updatedTeacher = userRepository.findById(teacherId).orElseThrow(
                    () -> new UserNotFoundException(teacherId));
            updatedClass.setTeacher(updatedTeacher);
        } else {
            updatedClass.setTeacher(null);
        }

        updatedClass.setName(newName);
        updatedClass.setAcademicYear(newYear);

        return updatedClass;
    }

    @Transactional
    public void addStudentToClass(Long classId, Long studentId) {

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
    }

    @Transactional
    public void removeStudentFromClass(Long classId, Long studentId) {

        SchoolClass schoolClass = getClassOrThrow(classId);

        User student = userRepository.findById(studentId).orElseThrow(
                () -> new UserNotFoundException(studentId));

        schoolClass.getStudents().remove(student);
    }

    @Transactional(readOnly = true)
    public SchoolClass getSchoolClass(Long classId) {

        return getClassOrThrow(classId);
    }

    @Transactional(readOnly = true)
    public List<SchoolClass> getAllClasses(String academicYear) {

        return schoolClassRepository.findAllByAcademicYear(academicYear);
    }

    private SchoolClass getClassOrThrow(Long classId) {
        return schoolClassRepository.findById(classId).orElseThrow(
                () -> new SchoolClassNotFoundException(classId));
    }
}

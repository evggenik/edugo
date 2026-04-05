package com.evggenn.edugo.schoolclass;

import com.evggenn.edugo.exception.SchoolClassAlreadyExistsException;
import com.evggenn.edugo.exception.SchoolClassNotFoundException;
import com.evggenn.edugo.exception.UserNotFoundException;
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

        SchoolClass updatedClass = schoolClassRepository.findById(id).orElseThrow(
                () -> new SchoolClassNotFoundException(id)
        );

        if (schoolClassRepository.existsByNameAndAcademicYear(newName, newYear) &&
                !(updatedClass.getName().equalsIgnoreCase(newName) &&
                        updatedClass.getAcademicYear().equals(newYear))) {
            throw new SchoolClassAlreadyExistsException(newName, newYear);
        }

        User updatedTeacher = userRepository.findById(teacherId).orElseThrow(
                () -> new UserNotFoundException(teacherId));

        updatedClass.setName(newName);
        updatedClass.setAcademicYear(newYear);
        updatedClass.setTeacher(updatedTeacher);

        return schoolClassRepository.save(updatedClass);
    }

    @Transactional
    public void addStudentToClass(Long classId, Long studentId) {

        SchoolClass schoolClass = schoolClassRepository.findById(classId).orElseThrow(
                () -> new SchoolClassNotFoundException(classId)
        );

        User student = userRepository.findById(studentId).orElseThrow(
                () -> new UserNotFoundException(studentId));

        schoolClass.getStudents().add(student);

        schoolClassRepository.save(schoolClass);
    }

    @Transactional
    public void removeStudentFromClass(Long classId, Long studentId) {

        SchoolClass schoolClass = schoolClassRepository.findById(classId).orElseThrow(
                () -> new SchoolClassNotFoundException(classId)
        );

        User student = userRepository.findById(studentId).orElseThrow(
                () -> new UserNotFoundException(studentId));

        schoolClass.getStudents().remove(student);

        schoolClassRepository.save(schoolClass);
    }

    @Transactional(readOnly = true)
    public SchoolClass getSchoolClass(Long classId) {

        return schoolClassRepository.findById(classId).orElseThrow(
                () -> new SchoolClassNotFoundException(classId)
        );
    }

    @Transactional(readOnly = true)
    public List<SchoolClass> getAllClasses(String academicYear) {

        return schoolClassRepository.findAllByAcademicYear(academicYear);
    }
}

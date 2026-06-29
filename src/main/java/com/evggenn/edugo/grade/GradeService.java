package com.evggenn.edugo.grade;

import com.evggenn.edugo.grade.exception.*;
import com.evggenn.edugo.lesson.Lesson;
import com.evggenn.edugo.lesson.LessonRepository;
import com.evggenn.edugo.lesson.LessonStatus;
import com.evggenn.edugo.lesson.exception.LessonNotFoundException;
import com.evggenn.edugo.subject.Subject;
import com.evggenn.edugo.subject.SubjectRepository;
import com.evggenn.edugo.subject.exception.SubjectNotFoundException;
import com.evggenn.edugo.term.Term;
import com.evggenn.edugo.term.TermRepository;
import com.evggenn.edugo.term.exception.TermNotFoundException;
import com.evggenn.edugo.user.RoleName;
import com.evggenn.edugo.user.User;
import com.evggenn.edugo.user.UserService;
import com.evggenn.edugo.util.AcademicYearUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
public class GradeService {

    private final GradeRepository gradeRepository;

    private final UserService userService;

    private final LessonRepository  lessonRepository;

    private final TermRepository termRepository;

    private final SubjectRepository subjectRepository;

    @Transactional
    public Grade createGrade(
            Short value,
            GradeType type,
            String comment,
            Long studentId,
            Long lessonId,
            Long termId,
            Long subjectId,
            Long currentUserId) {

        Lesson lesson = null;
        Term term = null;
        Subject subject = null;

        User student = userService.findStudentByIdOrThrow(studentId);

        if (type == GradeType.LESSON) {
            if (lessonId == null) throw new InvalidLessonGradeException(type);

            // TODO: filter by current academic year
            //  via findByIdAndAcademicYear (like HomeworkService)
            lesson = lessonRepository.findByIdWithSubject(lessonId)
                    .orElseThrow(() -> new LessonNotFoundException(lessonId));

            if (!lesson.getTeacher().getId().equals(currentUserId)) {
                throw new AccessDeniedException(
                        "You can only create grades for your own lessons");
            }

            subject = lesson.getSubject();
        } else {
            if (termId == null) throw new InvalidFinalGradeException(type);

            term = termRepository.findById(termId)
                    .orElseThrow(() -> new TermNotFoundException(termId));

            subject = subjectRepository.findById(subjectId)
                    .orElseThrow(() -> new SubjectNotFoundException(subjectId));
        }

        Grade grade = Grade.builder()
                .value(value)
                .type(type)
                .comment(comment)
                .student(student)
                .lesson(lesson)
                .term(term)
                .subject(subject)
                .build();

        return gradeRepository.save(grade);
    }

    @Transactional
    public void updateGrade(Long id, Short value, String comment, Long currentUserId) {

        Grade grade = getGradeOrThrow(id);

        if (grade.getType() == GradeType.LESSON) {
            validateLessonGradeAccess(grade, currentUserId);
        } else {
            validateFinalGradeAccess(grade, currentUserId);
        }

        if (value != null) grade.setValue(value);
        if (comment != null) grade.setComment(comment);
    }

    @Transactional
    public void deleteGrade(Long id, Long currentUserId) {

        Grade grade = getGradeOrThrow(id);

        if (grade.getType() == GradeType.LESSON) {
            validateLessonGradeAccess(grade, currentUserId);
        } else {
            validateFinalGradeAccess(grade, currentUserId);
        }

        gradeRepository.delete(grade);
    }

    @Transactional(readOnly = true)
    public List<Grade> getGradesBySubjectTermAndStudent(
            Long subjectId, Long termId, Long studentId, Long currentUserId) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isStudent = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(RoleName.STUDENT.name()));

        // TODO: add PARENT access check via parent_students relationship
        // requires User.children and User.parents fields to be mapped in User entity

        if (isStudent && !currentUserId.equals(studentId)) {
            throw new AccessDeniedException("You can only view your own grades");
        }

        subjectRepository.findById(subjectId)
                .orElseThrow(() -> new SubjectNotFoundException(subjectId));

        termRepository.findByIdAndAcademicYear(
                        termId,
                        AcademicYearUtil.getCurrentAcademicYear())
                .orElseThrow(() -> new TermNotFoundException(termId));

        userService.findStudentByIdOrThrow(studentId);

        return gradeRepository.findAllWithDetailsBySubjectAndTermAndStudent(
                subjectId, termId, studentId
        );
    }

    @Transactional(readOnly = true)
    public Grade getGrade(Long id, Long currentUserId) {

        Grade grade = gradeRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new GradeNotFoundException(id));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isStudent = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(RoleName.STUDENT.name()));

        if (isStudent && !grade.getStudent().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You can only view your own grades");
        }
        // TODO: add PARENT access check via parent_students relationship
        // requires User.children and User.parents fields to be mapped in User entity

        return grade;
    }

    private Grade getGradeOrThrow(Long id) {
        return gradeRepository.findById(id)
                .orElseThrow(() -> new GradeNotFoundException(id));
    }

    private void validateLessonGradeAccess(Grade grade, Long currentUserId) {
        Lesson lesson = grade.getLesson();

        if (lesson.getStatus() != LessonStatus.COMPLETED) {
            throw new GradeNotEditableException(lesson.getStatus());
        }

        if (!lesson.getTeacher().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You can only update your own grades");
        }
    }

    private void validateFinalGradeAccess(Grade grade, Long currentUserId) {
        // TODO: access check for final grades (QUARTER, YEAR, EXAM)
        // requires student -> SchoolClass -> teacher relationship
    }
}

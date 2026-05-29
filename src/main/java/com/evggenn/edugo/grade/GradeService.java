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
import com.evggenn.edugo.user.User;
import com.evggenn.edugo.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


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
            Long subjectId) {

        Lesson lesson = null;
        Term term = null;
        Subject subject = null;

        User student = userService.findStudentByIdOrThrow(studentId);

        if (type == GradeType.LESSON) {
            if (lessonId == null) throw new InvalidLessonGradeException(type);

            lesson = lessonRepository.findById(lessonId)
                    .orElseThrow(() -> new LessonNotFoundException(lessonId));

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

        Grade grade = gradeRepository.findById(id).orElseThrow(
                () -> new GradeNotFoundException(id)
        );

        if (grade.getType() == GradeType.LESSON) {
            validateLessonGradeAccess(grade, currentUserId);
        } else {
            validateFinalGradeAccess(grade, currentUserId);
        }

        if (value != null) grade.setValue(value);
        if (comment != null) grade.setComment(comment);

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

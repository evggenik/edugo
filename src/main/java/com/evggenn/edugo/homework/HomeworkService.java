package com.evggenn.edugo.homework;

import com.evggenn.edugo.homework.exception.HomeworkAlreadyExistsException;
import com.evggenn.edugo.homework.exception.HomeworkNotFoundException;
import com.evggenn.edugo.homework.exception.LessonCancelledException;
import com.evggenn.edugo.lesson.Lesson;
import com.evggenn.edugo.lesson.LessonRepository;
import com.evggenn.edugo.lesson.LessonStatus;
import com.evggenn.edugo.lesson.exception.LessonNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class HomeworkService {

    private  final HomeworkRepository homeworkRepository;

    private  final LessonRepository lessonRepository;

    @Transactional
    public Homework createHomework(
            String description,
            LocalDate dueDate,
            Long lessonId,
            String academicYear,
            Long currentUserId) {

        Lesson lesson = lessonRepository.findByIdAndAcademicYear(lessonId, academicYear)
                .orElseThrow(() -> new LessonNotFoundException(lessonId));

        if (homeworkRepository.existsByLessonId(lessonId)) {
            throw new HomeworkAlreadyExistsException(lessonId);
        }

        if (!lesson.getTeacher().getId().equals(currentUserId)) {
            throw new AccessDeniedException(
                    "You can only assign homeworks for your own lessons"
            );
        }

        if (lesson.getStatus() == LessonStatus.CANCELLED) {
            throw new LessonCancelledException(lesson.getStatus());
        }

        Homework homework = Homework.builder()
                .description(description)
                .dueDate(dueDate)
                .lesson(lesson)
                .build();

        return homeworkRepository.save(homework);
    }

    @Transactional
    public void updateHomework(
            Long id,
            String description,
            LocalDate dueDate,
            Long currentUserId) {

        Homework homework = homeworkRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new HomeworkNotFoundException(id));

        if (!homework.getLesson().getTeacher().getId().equals(currentUserId)) {
            throw new AccessDeniedException(
                    "You can only update homeworks for your own lessons"
            );
        }

        if (description != null) homework.setDescription(description);
        if (dueDate != null) homework.setDueDate(dueDate);
    }

    @Transactional(readOnly = true)
    public Homework getHomework(Long id) {

        return homeworkRepository.findById(id)
                .orElseThrow(() -> new HomeworkNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public Homework getHomeworkByLesson(Long lessonId) {

        return homeworkRepository.findByLessonId(lessonId)
                .orElseThrow(() -> HomeworkNotFoundException.byLesson(lessonId));
    }
}

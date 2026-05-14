package com.evggenn.edugo.lesson;

import com.evggenn.edugo.lesson.exception.*;
import com.evggenn.edugo.schoolclass.exception.ClassIsArchivedException;
import com.evggenn.edugo.subject.SubjectRepository;
import com.evggenn.edugo.subject.exception.SubjectNotFoundException;
import com.evggenn.edugo.term.TermRepository;
import com.evggenn.edugo.term.exception.TermNotFoundException;
import com.evggenn.edugo.user.UserService;
import com.evggenn.edugo.schoolclass.SchoolClass;
import com.evggenn.edugo.schoolclass.exception.SchoolClassNotFoundException;
import com.evggenn.edugo.schoolclass.SchoolClassRepository;
import com.evggenn.edugo.subject.Subject;
import com.evggenn.edugo.term.Term;
import com.evggenn.edugo.user.User;
import com.evggenn.edugo.user.exception.TeacherDoesNotTeachSubjectException;
import com.evggenn.edugo.util.AcademicYearUtil;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.TriConsumer;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LessonService {

    private final LessonRepository lessonRepository;

    private final SchoolClassRepository  schoolClassRepository;

    private final SubjectRepository subjectRepository;

    private final TermRepository termRepository;

    private final UserService userService;

    @Transactional
    public Lesson createLesson(String topic,
                               LocalDateTime startTime,
                               LocalDateTime endTime,
                               String room,
                               Long schoolClassId,
                               Long subjectId,
                               Long teacherId,
                               Long  termId) {

        String currentAcademicYear = AcademicYearUtil.getCurrentAcademicYear();

        if (!startTime.isBefore(endTime)) {
            throw new InvalidTimesException(startTime, endTime);
        }

        SchoolClass schoolClass = schoolClassRepository.findById(schoolClassId).orElseThrow(
                () -> new SchoolClassNotFoundException(schoolClassId)
        );

        if (!schoolClass.getAcademicYear().equals(currentAcademicYear)) {
            throw new ClassIsArchivedException(schoolClass.getAcademicYear());
        }

        User teacher = userService.findTeacherByIdOrThrow(teacherId);

        Subject subject = subjectRepository.findById(subjectId).orElseThrow(
                () -> new SubjectNotFoundException(subjectId)
        );

        if (!teacher.getSubjects().contains(subject)) {
            throw new TeacherDoesNotTeachSubjectException(teacherId, subjectId);
        }

        Term term = termRepository.findByIdAndAcademicYear(
                termId, currentAcademicYear).orElseThrow(
                () -> new TermNotFoundException(termId, currentAcademicYear)
        );

        if (startTime.toLocalDate().isBefore(term.getStartDate()) ||
                startTime.toLocalDate().isAfter(term.getEndDate())) {
            throw new LessonOutOfTermException(term.getName());
        }

        if (lessonRepository.existsOverlappingTimes(schoolClassId, startTime, endTime)) {
            throw new LessonConflictException(LessonConflictType.CLASS_OCCUPIED);
        }

        if (lessonRepository.existsOverlappingByTeacher(teacherId, startTime, endTime)) {
            throw new LessonConflictException(LessonConflictType.TEACHER_BUSY);
        }

        Lesson lesson = Lesson.builder()
                .topic(topic)
                .startTime(startTime)
                .endTime(endTime)
                .room(room)
                .schoolClass(schoolClass)
                .subject(subject)
                .teacher(teacher)
                .term(term)
                .build();

        return lessonRepository.save(lesson);
    }

    @Transactional
    public void updateLesson(
            Long id,
            String topic,
            LocalDateTime startTime,
            LocalDateTime endTime,
            String room,
            Long schoolClassId,
            Long subjectId,
            Long teacherId,
            Long  termId) {

        Lesson lesson = lessonRepository.findById(id).orElseThrow(
                () -> new LessonNotFoundException(id)
        );

        if (lesson.getStatus() != LessonStatus.SCHEDULED) {
            throw new LessonNotEditableException(lesson.getStatus().name());
        }

        String currentAcademicYear = AcademicYearUtil.getCurrentAcademicYear();

        if (!startTime.isBefore(endTime)) {
            throw new InvalidTimesException(startTime, endTime);
        }

        SchoolClass schoolClass = schoolClassRepository.findById(schoolClassId).orElseThrow(
                () -> new SchoolClassNotFoundException(schoolClassId)
        );

        if (!schoolClass.getAcademicYear().equals(currentAcademicYear)) {
            throw new ClassIsArchivedException(schoolClass.getAcademicYear());
        }

        User teacher = userService.findTeacherByIdOrThrow(teacherId);

        Subject subject = subjectRepository.findById(subjectId).orElseThrow(
                () -> new SubjectNotFoundException(subjectId)
        );

        if (!teacher.getSubjects().contains(subject)) {
            throw new TeacherDoesNotTeachSubjectException(teacherId, subjectId);
        }

        Term term = termRepository.findByIdAndAcademicYear(
                termId, currentAcademicYear).orElseThrow(
                () -> new TermNotFoundException(termId, currentAcademicYear)
        );

        if (startTime.toLocalDate().isBefore(term.getStartDate()) ||
                startTime.toLocalDate().isAfter(term.getEndDate())) {
            throw new LessonOutOfTermException(term.getName());
        }

        if (lessonRepository.existsOverlappingTimesExcludingId(id, schoolClassId, startTime, endTime)) {
            throw new LessonConflictException(LessonConflictType.CLASS_OCCUPIED);
        }

        if (lessonRepository.existsOverlappingByTeacherExcludingId(id, teacherId, startTime, endTime)) {
            throw new LessonConflictException(LessonConflictType.TEACHER_BUSY);
        }

        lesson.setTopic(topic);
        lesson.setStartTime(startTime);
        lesson.setEndTime(endTime);
        lesson.setRoom(room);
        lesson.setSchoolClass(schoolClass);
        lesson.setSubject(subject);
        lesson.setTeacher(teacher);
        lesson.setTerm(term);
    }

    @Transactional(readOnly = true)
    public List<Lesson> findLessonsByTeacherClassAndTerm(
            Long teacherId, Long schoolClassId, Long termId) {
        return lessonRepository
                .findLessonsByTeacherClassAndTerm(teacherId, schoolClassId, termId);
    }

    @Transactional(readOnly = true)
    public Lesson getLessonById(Long id) {
        return lessonRepository.findByIdWithDetails(id).orElseThrow(
                () -> new LessonNotFoundException(id));
    }

    @Transactional
    public void updateLessonContent(Long lessonId, Long currentUserId, String newTopic, String newRoom) {
        Lesson lesson = lessonRepository.findById(lessonId).orElseThrow(
                () -> new LessonNotFoundException(lessonId)
        );

        if (!lesson.getTeacher().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You can only update your own lessons");
        }

        if (newTopic != null) lesson.setTopic(newTopic);
        if (newRoom != null) lesson.setRoom(newRoom);
    }
}

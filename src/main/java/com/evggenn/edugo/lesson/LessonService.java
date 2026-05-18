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
import com.evggenn.edugo.user.exception.UserNotFoundException;
import com.evggenn.edugo.util.AcademicYearUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Manages lesson scheduling: creation, updates, retrieval, and status transitions.
 *
 * <p>Enforces two types of time conflict rules:
 * a class cannot have two overlapping lessons,
 * and a teacher cannot teach two overlapping lessons simultaneously.</p>
 *
 * <p>Academic year is always resolved internally via {@link AcademicYearUtil}
 * and is never expected from the caller.</p>
 *
 * <p>Lesson lifecycle: {@code SCHEDULED} → {@code COMPLETED} or {@code CANCELLED}.
 * Only {@code SCHEDULED} lessons can be edited or deleted.</p>
 */
@Service
@RequiredArgsConstructor
public class LessonService {

    private final LessonRepository lessonRepository;

    private final SchoolClassRepository  schoolClassRepository;

    private final SubjectRepository subjectRepository;

    private final TermRepository termRepository;

    private final UserService userService;

    /**
     * Creates a new lesson and persists it to the database.
     *
     * <p>Validates in order:</p>
     * <ol>
     *   <li>{@code startTime} is strictly before {@code endTime}</li>
     *   <li>school class belongs to the current academic year</li>
     *   <li>teacher exists and has the TEACHER role</li>
     *   <li>teacher is assigned to the given subject</li>
     *   <li>term belongs to the current academic year</li>
     *   <li>{@code startTime} falls within the term's date range</li>
     *   <li>no overlapping lesson exists for the class</li>
     *   <li>no overlapping lesson exists for the teacher</li>
     * </ol>
     *
     * @param topic         optional lesson topic
     * @param startTime     lesson start (must be strictly before {@code endTime})
     * @param endTime       lesson end
     * @param room          optional room identifier
     * @param schoolClassId ID of the school class
     * @param subjectId     ID of the subject
     * @param teacherId     ID of the teacher
     * @param termId        ID of the term within the current academic year
     * @return the created {@link Lesson} entity
     * @throws InvalidTimesException                 if {@code startTime >= endTime}
     * @throws SchoolClassNotFoundException          if no class found with the given ID
     * @throws ClassIsArchivedException              if the class belongs to a past academic year
     * @throws UserNotFoundException                 if no teacher found with the given ID
     * @throws SubjectNotFoundException              if no subject found with the given ID
     * @throws TeacherDoesNotTeachSubjectException   if the teacher is not assigned to this subject
     * @throws TermNotFoundException                 if no term found for the given ID in the current year
     * @throws LessonOutOfTermException              if {@code startTime} is outside the term's date range
     * @throws LessonConflictException               if the class or teacher has a conflicting lesson
     */
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

    /**
     * Fully replaces the scheduling data of an existing lesson.
     *
     * <p>Only lessons with status {@link LessonStatus#SCHEDULED} can be updated.
     * All conflict checks are performed excluding the lesson being updated.</p>
     *
     * @param id            the ID of the lesson to update
     * @param topic         new topic (may be null)
     * @param startTime     new start time
     * @param endTime       new end time
     * @param room          new room (may be null)
     * @param schoolClassId new school class ID
     * @param subjectId     new subject ID
     * @param teacherId     new teacher ID
     * @param termId        new term ID
     * @throws LessonNotFoundException               if no lesson found with the given ID
     * @throws LessonNotEditableException            if lesson status is not SCHEDULED
     * @throws InvalidTimesException                 if {@code startTime >= endTime}
     * @throws SchoolClassNotFoundException          if no class found with the given ID
     * @throws ClassIsArchivedException              if the class belongs to a past academic year
     * @throws UserNotFoundException                 if no teacher found with the given ID
     * @throws SubjectNotFoundException              if no subject found with the given ID
     * @throws TeacherDoesNotTeachSubjectException   if the teacher is not assigned to this subject
     * @throws TermNotFoundException                 if no term found for the given ID in the current year
     * @throws LessonOutOfTermException              if {@code startTime} is outside the term's date range
     * @throws LessonConflictException               if the class or teacher has a conflicting lesson
     */
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

    /**
     * Updates only the topic and room of a lesson taught by the current user.
     *
     * <p>Unlike {@link #updateLesson}, this method is intended for teachers —
     * only the content fields (topic, room) can be changed, not the schedule.
     * Null values are ignored (partial update semantics).</p>
     *
     * @param lessonId      the ID of the lesson to update
     * @param currentUserId the ID of the currently authenticated user
     * @param newTopic      new topic, or {@code null} to keep the existing value
     * @param newRoom       new room, or {@code null} to keep the existing value
     * @throws LessonNotFoundException if no lesson found with the given ID
     * @throws AccessDeniedException   if the current user is not the lesson's teacher
     */
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

    /**
     * Returns a single lesson with all associations loaded (class, teacher, subject, term).
     *
     * @param id the lesson ID
     * @return the found {@link Lesson} entity
     * @throws LessonNotFoundException if no lesson found with the given ID
     */
    @Transactional(readOnly = true)
    public Lesson getLessonById(Long id) {
        return lessonRepository.findByIdWithDetails(id).orElseThrow(
                () -> new LessonNotFoundException(id));
    }

    /**
     * Returns lessons for a specific teacher, class, and term combination.
     *
     * <p>All three entities are validated before querying.</p>
     *
     * @param teacherId     the ID of the teacher
     * @param schoolClassId the ID of the school class
     * @param termId        the ID of the term (must belong to the current academic year)
     * @return list of lessons, may be empty
     * @throws UserNotFoundException        if no teacher found with the given ID
     * @throws SchoolClassNotFoundException if no class found with the given ID
     * @throws TermNotFoundException        if no term found in the current academic year
     */
    @Transactional(readOnly = true)
    public List<Lesson> getLessonsByTeacherClassAndTerm(
            Long teacherId, Long schoolClassId, Long termId) {

        userService.findTeacherByIdOrThrow(teacherId);

        if (!schoolClassRepository.existsById(schoolClassId)) {
            throw new SchoolClassNotFoundException(schoolClassId);
        }

        String currentYear = AcademicYearUtil.getCurrentAcademicYear();
        termRepository.findByIdAndAcademicYear(termId, currentYear)
                .orElseThrow(() -> new TermNotFoundException(termId, currentYear));

        return lessonRepository
                .findLessonsByTeacherClassAndTerm(teacherId, schoolClassId, termId);
    }

    /**
     * Returns lessons for a class within a date range.
     *
     * @param classId the ID of the school class
     * @param from    start of the range (inclusive)
     * @param to      end of the range (exclusive)
     * @return list of lessons ordered by {@code startTime}, may be empty
     * @throws SchoolClassNotFoundException if no class found with the given ID
     * @throws InvalidDateRangeException    if {@code from >= to} or range exceeds 31 days
     */
    @Transactional(readOnly = true)
    public List<Lesson> getLessonsByClass(Long classId, LocalDateTime from,
                                          LocalDateTime to) {
        validateDateRange(from, to);

        if (!schoolClassRepository.existsById(classId)) {
            throw new SchoolClassNotFoundException(classId);
        }

        return lessonRepository
                .findAllBySchoolClassIdAndStartTimeBetween(classId, from, to);
    }

    /**
     * Returns lessons for a teacher within a date range.
     *
     * @param teacherId the ID of the teacher
     * @param from      start of the range (inclusive)
     * @param to        end of the range (exclusive)
     * @return list of lessons ordered by {@code startTime}, may be empty
     * @throws UserNotFoundException      if no teacher found with the given ID
     * @throws InvalidDateRangeException  if {@code from >= to} or range exceeds 31 days
     */
    @Transactional(readOnly = true)
    public List<Lesson> getLessonsByTeacher(Long teacherId, LocalDateTime from,
                                            LocalDateTime to) {
        validateDateRange(from, to);

        userService.findTeacherByIdOrThrow(teacherId);

        return lessonRepository
                .findAllByTeacherIdAndStartTimeBetween(teacherId, from, to);
    }

    /**
     * Marks a lesson as {@link LessonStatus#COMPLETED}.
     *
     * <p>Only the teacher assigned to the lesson can complete it.</p>
     *
     * @param lessonId      the ID of the lesson
     * @param currentUserId the ID of the currently authenticated user
     * @throws LessonNotFoundException       if no lesson found with the given ID
     * @throws AccessDeniedException         if the current user is not the lesson's teacher
     * @throws InvalidLessonStatusException  if lesson status is not SCHEDULED
     */
    @Transactional
    public void completeLesson(Long lessonId, Long currentUserId) {
        Lesson lesson = getLessonTaughtByCurrentUser(lessonId, currentUserId);

        if (lesson.getStatus() != LessonStatus.SCHEDULED) {
            throw new InvalidLessonStatusException(lesson.getStatus());
        }

        lesson.setStatus(LessonStatus.COMPLETED);
    }

    /**
     * Marks a lesson as {@link LessonStatus#CANCELLED}.
     *
     * <p>Only the teacher assigned to the lesson can cancel it.</p>
     *
     * @param lessonId      the ID of the lesson
     * @param currentUserId the ID of the currently authenticated user
     * @throws LessonNotFoundException       if no lesson found with the given ID
     * @throws AccessDeniedException         if the current user is not the lesson's teacher
     * @throws InvalidLessonStatusException  if lesson status is not SCHEDULED
     */
    @Transactional
    public void cancelLesson(Long lessonId, Long currentUserId) {
        Lesson lesson = getLessonTaughtByCurrentUser(lessonId, currentUserId);

        if (lesson.getStatus() != LessonStatus.SCHEDULED) {
            throw new InvalidLessonStatusException(lesson.getStatus());
        }

        lesson.setStatus(LessonStatus.CANCELLED);
    }

    /**
     * Permanently deletes a lesson.
     *
     * <p>Only {@link LessonStatus#SCHEDULED} lessons can be deleted.
     * Completed or cancelled lessons are kept for historical records.</p>
     *
     * @param lessonId the ID of the lesson to delete
     * @throws LessonNotFoundException          if no lesson found with the given ID
     * @throws LessonDeletionNotAllowedException if lesson status is not SCHEDULED
     */
    @Transactional
    public void deleteLesson(Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId).orElseThrow(
                () -> new LessonNotFoundException(lessonId)
        );

        if (lesson.getStatus() != LessonStatus.SCHEDULED) {
            throw new LessonDeletionNotAllowedException(lesson.getStatus());
        }

        lessonRepository.delete(lesson);
    }

    private void validateDateRange(LocalDateTime from, LocalDateTime to) {
        if (!from.isBefore(to)) {
            throw new InvalidDateRangeException("'from' must be before 'to'");
        }
        if (ChronoUnit.DAYS.between(from, to) > 31) {
            throw new InvalidDateRangeException("The period of time cannot exceed 31 days");
        }
    }

    private Lesson getLessonTaughtByCurrentUser(Long lessonId, Long currentUserId) {
        Lesson lesson = lessonRepository.findById(lessonId).orElseThrow(
                () -> new LessonNotFoundException(lessonId)
        );

        if (!lesson.getTeacher().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You can only update your own lessons");
        }

        return lesson;
    }
}

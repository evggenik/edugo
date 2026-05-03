package com.evggenn.edugo.lesson;

import com.evggenn.edugo.lesson.exception.InvalidTimesException;
import com.evggenn.edugo.lesson.exception.LessonConflictException;
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
import com.evggenn.edugo.user.UserRepository;
import com.evggenn.edugo.util.AcademicYearUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LessonService {

    private final LessonRepository lessonRepository;

    private final SchoolClassRepository  schoolClassRepository;

    private final UserRepository userRepository;

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

        Term term = termRepository.findByIdAndAcademicYear(
                termId, currentAcademicYear).orElseThrow(
                () -> new TermNotFoundException(termId, currentAcademicYear)
        );

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
}

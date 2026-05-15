package com.evggenn.edugo.exception;

import com.evggenn.edugo.lesson.exception.*;
import com.evggenn.edugo.schoolclass.exception.SchoolClassAlreadyExistsException;
import com.evggenn.edugo.schoolclass.exception.SchoolClassNotFoundException;
import com.evggenn.edugo.schoolclass.exception.StudentAlreadyInClassException;
import com.evggenn.edugo.subject.exception.SubjectAlreadyExistsException;
import com.evggenn.edugo.subject.exception.SubjectNotFoundException;
import com.evggenn.edugo.term.exception.InvalidTermDatesException;
import com.evggenn.edugo.term.exception.TermAlreadyExistsException;
import com.evggenn.edugo.term.exception.TermNotFoundException;
import com.evggenn.edugo.term.exception.TermOverlapException;
import com.evggenn.edugo.user.exception.*;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({InvalidLessonStatusException.class, LessonDeletionNotAllowedException.class})
    public ResponseEntity<Map<String, String>> handleLessonStatusExceptions(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDeniedToUpdateLesson(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(LessonOutOfTermException.class)
    public ResponseEntity<Map<String, String>> handleLessonOutOfTerm(LessonOutOfTermException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(TeacherDoesNotTeachSubjectException.class)
    public ResponseEntity<Map<String, String>> handleTeacherDoesNotTeachSubject(TeacherDoesNotTeachSubjectException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(LessonNotEditableException.class)
    public ResponseEntity<Map<String, String>> handleLessonNotEditable(LessonNotEditableException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(LessonNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleLessonNotFound(LessonNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(NotTeacherException.class)
    public ResponseEntity<Map<String, String>> handleNotTeacher(NotTeacherException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(TeacherAlreadyHasSubjectException.class)
    public ResponseEntity<Map<String, String>> handleTeacherHasSubject(TeacherAlreadyHasSubjectException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(TeacherHasNotSubjectException.class)
    public ResponseEntity<Map<String, String>> handleTeacherHasNotSubject(TeacherHasNotSubjectException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(SchoolRoleNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleRoleNotFound(SchoolRoleNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(InvalidTimesException.class)
    public ResponseEntity<Map<String, String>> handleInvalidTimes(InvalidTimesException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(LessonConflictException.class)
    public ResponseEntity<Map<String, String>> handleLessonTimeOverlap(LessonConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleInvalidFormat(HttpMessageNotReadableException ex) {
        String message = "Invalid request format";

        Throwable cause = ex.getCause();
        if (cause instanceof InvalidFormatException ife) {
            if (ife.getTargetType().isEnum()) {
                message = String.format("Invalid value '%s' for field '%s'. Allowed values: %s",
                        ife.getValue(),
                        ife.getPath().get(0).getFieldName(),
                        Arrays.toString(ife.getTargetType().getEnumConstants()));
            }
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", message));
    }

    @ExceptionHandler(InvalidTermDatesException.class)
    public ResponseEntity<Map<String, String>> handlePeriodOverlap(InvalidTermDatesException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(TermOverlapException.class)
    public ResponseEntity<Map<String, String>> handlePeriodOverlap(TermOverlapException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(TermAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handlePeriodAlreadyExists(TermAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(TermNotFoundException.class)
    public ResponseEntity<Map<String, String>> handlePeriodNotFound(TermNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(StudentAlreadyInClassException.class)
    public ResponseEntity<Map<String, String>> handleStudentAlreadyInClass(StudentAlreadyInClassException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(NotStudentException.class)
    public ResponseEntity<Map<String, String>> handleNotStudent(NotStudentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(SchoolClassNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleSchoolClassNotFound(SchoolClassNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(SchoolClassAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleSchoolClassExists(SchoolClassAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(SubjectAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleSubjectExists(SubjectAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(SubjectNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleSubjectNotFound(SubjectNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleEmailExists(EmailAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Invalid email or password"));
    }

    @ExceptionHandler(InternalAuthenticationServiceException.class)
    public ResponseEntity<Map<String, String>> handleInternalAuth(InternalAuthenticationServiceException ex) {
        if (ex.getCause() instanceof UserNotFoundException) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid email or password"));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Internal server error"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleOther(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Internal server error"));
    }
}

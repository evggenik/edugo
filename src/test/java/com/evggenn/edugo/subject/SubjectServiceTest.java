package com.evggenn.edugo.subject;

import com.evggenn.edugo.exception.SubjectAlreadyExistsException;
import com.evggenn.edugo.exception.SubjectNotFoundException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubjectServiceTest {

    @Mock
    private SubjectRepository subjectRepository;

    @InjectMocks
    private SubjectService subjectService;

    @Test
    void createSubject_shouldReturnSubject_whenNameNotExists() {
        String subjectName = "Math";
        Subject expectedSubject = new Subject(subjectName);

        when(subjectRepository.existsByNameIgnoreCase(subjectName)).thenReturn(false);
        when(subjectRepository.save(any(Subject.class))).thenReturn(expectedSubject);

        Subject result = subjectService.createSubject(subjectName);

        assertThat(result.getName()).isEqualTo(subjectName);
    }

    @Test
    void createSubject_shouldThrowSubjectAlreadyExistsException_whenNameExists() {
        String subjectName = "Math";

        when(subjectRepository.existsByNameIgnoreCase(subjectName)).thenReturn(true);

        Throwable throwable = assertThrows(SubjectAlreadyExistsException.class, () -> {
            subjectService.createSubject(subjectName);
        });
        assertThat(throwable.getMessage()).isEqualTo("Subject already exists: " + subjectName);
    }

    @Test
    void updateSubject_shouldReturnSubject_whenIdExistsAndNewNameIsUnique() {
        Long id = 1L;
        Subject subject = new Subject("Math");

        when(subjectRepository.findById(id)).thenReturn(Optional.of(subject));
        when(subjectRepository.save(subject)).thenReturn(subject);

        Subject updatedSubject = subjectService.updateSubject(id, "Art");
        assertThat(updatedSubject.getName()).isEqualTo("Art");
    }

    @Test
    void updateSubject_shouldThrowSubjectAlreadyExistsException_whenNewNameExists() {
        Long id = 1L;
        Subject subject = new Subject("Math");

        when(subjectRepository.findById(id)).thenReturn(Optional.of(subject));
        when(subjectRepository.existsByNameIgnoreCase("Art")).thenReturn(true);

        Throwable throwable = assertThrows(SubjectAlreadyExistsException.class, () -> {
            subjectService.updateSubject(id, "Art");
        });
        assertThat(throwable.getMessage()).isEqualTo("Subject already exists: Art");
    }

    @Test
    void updateSubject_shouldThrowSubjectNotFoundException_whenIdNotExists() {
        Long id = 1L;

        when(subjectRepository.findById(id)).thenReturn(Optional.empty());

        Throwable throwable = assertThrows(SubjectNotFoundException.class, () -> {
            subjectService.updateSubject(id, "Art");
        });
        assertThat(throwable.getMessage()).isEqualTo("Subject not found with id: 1");
    }

    @Test
    void updateSubject_shouldReturnSubject_whenNewNameIsSameAsOld() {
        Long id = 1L;
        Subject subject = new Subject("Math");

        when(subjectRepository.findById(id)).thenReturn(Optional.of(subject));
        when(subjectRepository.save(subject)).thenReturn(subject);

        Subject updatedSubject = subjectService.updateSubject(id, "Math");

        assertThat(updatedSubject.getName()).isEqualTo("Math");
    }


    @Test
    void getSubject_shouldReturnSubject_whenIdExists() {
        Long id = 1L;
        Subject subject = new Subject("Math");

        when(subjectRepository.findById(id)).thenReturn(Optional.of(subject));

        Subject result = subjectService.getSubject(id);

        assertThat(result.getName()).isEqualTo("Math");
    }

    @Test
    void getSubject_shouldThrowSubjectNotFoundException_whenIdNotExists() {
        Long id = 1L;

        when(subjectRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(SubjectNotFoundException.class, () -> {
            subjectService.getSubject(id);
        });
    }

    @Test
    void getAllSubjects_shouldReturnListOfSubjects() {
        Subject subject1 = new Subject();
        Subject subject2 = new Subject();

        when(subjectRepository.findAll()).thenReturn(List.of(subject1, subject2));

        List<Subject> resultList = subjectService.getAllSubjects();

        assertThat(resultList).containsExactlyInAnyOrder(subject1, subject2);
    }
}
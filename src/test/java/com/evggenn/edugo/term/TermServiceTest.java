package com.evggenn.edugo.term;

import com.evggenn.edugo.term.exception.InvalidTermDatesException;
import com.evggenn.edugo.term.exception.TermAlreadyExistsException;
import com.evggenn.edugo.term.exception.TermNotFoundException;
import com.evggenn.edugo.term.exception.TermOverlapException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TermServiceTest {

    @Mock
    private TermRepository termRepository;

    @InjectMocks
    private TermService termService;

    private static final String CURRENT_YEAR = "2025-2026";

    @Test
    void createPeriod_shouldCreate_whenValid() {
        LocalDate newStart = LocalDate.of(2025, 9, 1);
        LocalDate newEnd = LocalDate.of(2025, 10, 31);


        when(termRepository.existsOverlappingPeriod(CURRENT_YEAR, newStart, newEnd))
                .thenReturn(false);
        when(termRepository.existsByNameAndAcademicYear("1 четверть", CURRENT_YEAR))
                .thenReturn(false);
        when(termRepository.save(any(Term.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ArgumentCaptor<Term> captor = ArgumentCaptor.forClass(Term.class);

        termService.createPeriod("1 четверть",  newStart, newEnd);

        verify(termRepository).existsByNameAndAcademicYear("1 четверть", CURRENT_YEAR);
        verify(termRepository).existsOverlappingPeriod(CURRENT_YEAR, newStart, newEnd);
        verify(termRepository).save(captor.capture());

        Term saved = captor.getValue();

        assertThat(saved.getName()).isEqualTo("1 четверть");
        assertThat(saved.getStartDate()).isEqualTo(newStart);
        assertThat(saved.getEndDate()).isEqualTo(newEnd);
        assertThat(saved.getAcademicYear()).isEqualTo(CURRENT_YEAR);
    }

    @Test
    void createPeriod_shouldThrow_whenDatesInvalid() {
        LocalDate newStart = LocalDate.of(2025, 10, 1);
        LocalDate newEnd = LocalDate.of(2025, 1, 31);

        assertThatThrownBy(() -> termService.createPeriod("1 четверть", newStart, newEnd))
                .isInstanceOf(InvalidTermDatesException.class);

        verifyNoInteractions(termRepository);
    }

    @Test
    void createPeriod_shouldThrow_whenNameAlreadyExists() {
        LocalDate newStart = LocalDate.of(2025, 1, 1);
        LocalDate newEnd = LocalDate.of(2025, 10, 31);

        when(termRepository.existsByNameAndAcademicYear("1 четверть", CURRENT_YEAR)).thenReturn(true);

        assertThatThrownBy(() -> termService.createPeriod("1 четверть", newStart, newEnd))
                .isInstanceOf(TermAlreadyExistsException.class)
                .hasMessageContaining("1 четверть");

        verify(termRepository, never())
                .existsOverlappingPeriod(any(), any(), any());
        verify(termRepository, never()).save(any());
    }

    @Test
    void createPeriod_shouldThrow_whenDatesOverlap() {
        LocalDate newStart = LocalDate.of(2025, 9, 10);
        LocalDate newEnd = LocalDate.of(2025, 11, 30);

        when(termRepository.existsByNameAndAcademicYear("2 четверть", CURRENT_YEAR)).thenReturn(false);
        when(termRepository.existsOverlappingPeriod(CURRENT_YEAR, newStart, newEnd)).thenReturn(true);

        assertThatThrownBy(() -> termService.createPeriod("2 четверть", newStart, newEnd))
                .isInstanceOf(TermOverlapException.class);

        verify(termRepository).existsByNameAndAcademicYear("2 четверть", CURRENT_YEAR);
        verify(termRepository).existsOverlappingPeriod(CURRENT_YEAR, newStart, newEnd);
        verify(termRepository, never()).save(any());
    }

    @Test
    void updatePeriod_shouldUpdate_whenValid() {
        Term existingTerm = new Term(
                "1 quarter",
                LocalDate.of(2025, 9, 1),
                LocalDate.of(2025, 10, 30),
                CURRENT_YEAR
        );
        Long updatedId = 1L;
        String newName = "1 QUARTER";
        LocalDate newStart = LocalDate.of(2025, 9, 2);
        LocalDate newEnd = LocalDate.of(2025, 10, 29);

        when(termRepository.existsOverlappingPeriodExcludingId(CURRENT_YEAR, newStart, newEnd, updatedId))
                .thenReturn(false);
        when(termRepository.findByIdAndAcademicYear(updatedId, CURRENT_YEAR))
                .thenReturn(Optional.of(existingTerm));
        when(termRepository.existsByNameAndAcademicYear(newName, CURRENT_YEAR))
                .thenReturn(false);

        Term result = termService.updatePeriod(updatedId, newName, newStart, newEnd);

        assertThat(result).isSameAs(existingTerm);
        assertThat(result.getName()).isEqualTo(newName);
        assertThat(result.getStartDate()).isEqualTo(newStart);
        assertThat(result.getEndDate()).isEqualTo(newEnd);
    }

    @Test
    void updatePeriod_shouldThrow_whenPeriodNotFound() {
        when(termRepository.findByIdAndAcademicYear(1L, CURRENT_YEAR))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> termService.updatePeriod(1L,
                "1 quarter",
                LocalDate.of(2025, 9, 2),
                LocalDate.of(2025, 9, 3)))
                .isInstanceOf(TermNotFoundException.class);

        verify(termRepository).findByIdAndAcademicYear(1L, CURRENT_YEAR);
        verify(termRepository, never()).existsOverlappingPeriodExcludingId(any(), any(), any(), any());
    }

    @Test
    void updatePeriod_shouldThrow_whenDatesOverlap() {
        Term existingTerm = new Term(
                "1 quarter",
                LocalDate.of(2025, 9, 1),
                LocalDate.of(2025, 10, 30),
                CURRENT_YEAR
        );

        Long updatedId = 1L;
        String newName = "1 QUARTER";
        LocalDate newStart = LocalDate.of(2025, 10, 25);
        LocalDate newEnd = LocalDate.of(2025, 11, 29);

        when(termRepository.findByIdAndAcademicYear(updatedId, CURRENT_YEAR))
                .thenReturn(Optional.of(existingTerm));

        when(termRepository.existsOverlappingPeriodExcludingId(CURRENT_YEAR, newStart, newEnd, updatedId))
                .thenReturn(true);

        when(termRepository.existsByNameAndAcademicYear(newName, CURRENT_YEAR))
                .thenReturn(false);

        assertThatThrownBy(() -> termService.updatePeriod(updatedId, newName, newStart, newEnd))
                .isInstanceOf(TermOverlapException.class);

        verify(termRepository).findByIdAndAcademicYear(1L, CURRENT_YEAR);
        verify(termRepository).existsOverlappingPeriodExcludingId(CURRENT_YEAR, newStart, newEnd, updatedId);
    }

    @Test
    void updatePeriod_shouldThrow_whenStartAfterEnd() {
        Long updatedId = 1L;
        String  newName = "1 QUARTER";
        LocalDate newStart = LocalDate.of(2025, 11, 25);
        LocalDate newEnd = LocalDate.of(2025, 10, 29);

        when(termRepository.findByIdAndAcademicYear(updatedId, CURRENT_YEAR))
                .thenReturn(Optional.of(new Term()));

        assertThatThrownBy(() -> termService.updatePeriod(updatedId, newName, newStart, newEnd))
                .isInstanceOf(InvalidTermDatesException.class);

        verify(termRepository).findByIdAndAcademicYear(1L, CURRENT_YEAR);
        verify(termRepository, never()).existsOverlappingPeriodExcludingId(CURRENT_YEAR, newStart, newEnd, updatedId);
    }

    @Test
    void getPeriod_shouldReturnPeriod_whenIdExists() {
        Term existingTerm = new Term();

        when(termRepository.findByIdAndAcademicYear(1L, CURRENT_YEAR)).thenReturn(Optional.of(existingTerm));

        Term result = termService.getPeriod(1L);

        assertThat(result).isSameAs(existingTerm);
    }

    @Test
    void getPeriod_shouldThrow_whenNotFound() {
        when(termRepository.findByIdAndAcademicYear(1L, CURRENT_YEAR))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> termService.getPeriod(1L))
                .isInstanceOf(TermNotFoundException.class);
    }

    @Test
    void getAllPeriods_shouldReturnList_whenPeriodsExist() {
        List<Term> existingTerms = List.of(new Term(), new Term());

        when(termRepository.findAllByAcademicYear(CURRENT_YEAR)).thenReturn(existingTerms);

        List<Term> result = termService.getAllPeriods();

        assertThat(result).isSameAs(existingTerms);
        assertThat(result).containsExactlyElementsOf(existingTerms);
    }

    @Test
    void getAllPeriods_shouldReturnEmptyList_whenNoPeriodsExist() {
        when(termRepository.findAllByAcademicYear(CURRENT_YEAR))
                .thenReturn(Collections.emptyList());

        List<Term> result = termService.getAllPeriods();

        assertThat(result).isEmpty();
    }
}
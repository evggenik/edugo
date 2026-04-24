package com.evggenn.edugo.period;

import com.evggenn.edugo.period.exception.InvalidPeriodDatesException;
import com.evggenn.edugo.period.exception.PeriodAlreadyExistsException;
import com.evggenn.edugo.period.exception.PeriodNotFoundException;
import com.evggenn.edugo.period.exception.PeriodOverlapException;
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
class PeriodServiceTest {

    @Mock
    private PeriodRepository periodRepository;

    @InjectMocks
    private PeriodService periodService;

    private static final String CURRENT_YEAR = "2025-2026";

    @Test
    void createPeriod_shouldCreate_whenValid() {
        LocalDate newStart = LocalDate.of(2025, 9, 1);
        LocalDate newEnd = LocalDate.of(2025, 10, 31);


        when(periodRepository.existsOverlappingPeriod(CURRENT_YEAR, newStart, newEnd))
                .thenReturn(false);
        when(periodRepository.existsByNameAndAcademicYear("1 четверть", CURRENT_YEAR))
                .thenReturn(false);
        when(periodRepository.save(any(Period.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ArgumentCaptor<Period> captor = ArgumentCaptor.forClass(Period.class);

        periodService.createPeriod("1 четверть",  newStart, newEnd);

        verify(periodRepository).existsByNameAndAcademicYear("1 четверть", CURRENT_YEAR);
        verify(periodRepository).existsOverlappingPeriod(CURRENT_YEAR, newStart, newEnd);
        verify(periodRepository).save(captor.capture());

        Period saved = captor.getValue();

        assertThat(saved.getName()).isEqualTo("1 четверть");
        assertThat(saved.getStartDate()).isEqualTo(newStart);
        assertThat(saved.getEndDate()).isEqualTo(newEnd);
        assertThat(saved.getAcademicYear()).isEqualTo(CURRENT_YEAR);
    }

    @Test
    void createPeriod_shouldThrow_whenDatesInvalid() {
        LocalDate newStart = LocalDate.of(2025, 10, 1);
        LocalDate newEnd = LocalDate.of(2025, 1, 31);

        assertThatThrownBy(() -> periodService.createPeriod("1 четверть", newStart, newEnd))
                .isInstanceOf(InvalidPeriodDatesException.class);

        verifyNoInteractions(periodRepository);
    }

    @Test
    void createPeriod_shouldThrow_whenNameAlreadyExists() {
        LocalDate newStart = LocalDate.of(2025, 1, 1);
        LocalDate newEnd = LocalDate.of(2025, 10, 31);

        when(periodRepository.existsByNameAndAcademicYear("1 четверть", CURRENT_YEAR)).thenReturn(true);

        assertThatThrownBy(() -> periodService.createPeriod("1 четверть", newStart, newEnd))
                .isInstanceOf(PeriodAlreadyExistsException.class)
                .hasMessageContaining("1 четверть");

        verify(periodRepository, never())
                .existsOverlappingPeriod(any(), any(), any());
        verify(periodRepository, never()).save(any());
    }

    @Test
    void createPeriod_shouldThrow_whenDatesOverlap() {
        LocalDate newStart = LocalDate.of(2025, 9, 10);
        LocalDate newEnd = LocalDate.of(2025, 11, 30);

        when(periodRepository.existsByNameAndAcademicYear("2 четверть", CURRENT_YEAR)).thenReturn(false);
        when(periodRepository.existsOverlappingPeriod(CURRENT_YEAR, newStart, newEnd)).thenReturn(true);

        assertThatThrownBy(() -> periodService.createPeriod("2 четверть", newStart, newEnd))
                .isInstanceOf(PeriodOverlapException.class);

        verify(periodRepository).existsByNameAndAcademicYear("2 четверть", CURRENT_YEAR);
        verify(periodRepository).existsOverlappingPeriod(CURRENT_YEAR, newStart, newEnd);
        verify(periodRepository, never()).save(any());
    }

    @Test
    void updatePeriod_shouldUpdate_whenValid() {
        Period existingPeriod = new Period(
                "1 quarter",
                LocalDate.of(2025, 9, 1),
                LocalDate.of(2025, 10, 30),
                CURRENT_YEAR
        );
        Long updatedId = 1L;
        String newName = "1 QUARTER";
        LocalDate newStart = LocalDate.of(2025, 9, 2);
        LocalDate newEnd = LocalDate.of(2025, 10, 29);

        when(periodRepository.existsOverlappingPeriodExcludingId(CURRENT_YEAR, newStart, newEnd, updatedId))
                .thenReturn(false);
        when(periodRepository.findByIdAndAcademicYear(updatedId, CURRENT_YEAR))
                .thenReturn(Optional.of(existingPeriod));
        when(periodRepository.existsByNameAndAcademicYear(newName, CURRENT_YEAR))
                .thenReturn(false);

        Period result = periodService.updatePeriod(updatedId, newName, newStart, newEnd);

        assertThat(result).isSameAs(existingPeriod);
        assertThat(result.getName()).isEqualTo(newName);
        assertThat(result.getStartDate()).isEqualTo(newStart);
        assertThat(result.getEndDate()).isEqualTo(newEnd);
    }

    @Test
    void updatePeriod_shouldThrow_whenPeriodNotFound() {
        when(periodRepository.findByIdAndAcademicYear(1L, CURRENT_YEAR))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> periodService.updatePeriod(1L,
                "1 quarter",
                LocalDate.of(2025, 9, 2),
                LocalDate.of(2025, 9, 3)))
                .isInstanceOf(PeriodNotFoundException.class);

        verify(periodRepository).findByIdAndAcademicYear(1L, CURRENT_YEAR);
        verify(periodRepository, never()).existsOverlappingPeriodExcludingId(any(), any(), any(), any());
    }

    @Test
    void updatePeriod_shouldThrow_whenDatesOverlap() {
        Period existingPeriod = new Period(
                "1 quarter",
                LocalDate.of(2025, 9, 1),
                LocalDate.of(2025, 10, 30),
                CURRENT_YEAR
        );

        Long updatedId = 1L;
        String newName = "1 QUARTER";
        LocalDate newStart = LocalDate.of(2025, 10, 25);
        LocalDate newEnd = LocalDate.of(2025, 11, 29);

        when(periodRepository.findByIdAndAcademicYear(updatedId, CURRENT_YEAR))
                .thenReturn(Optional.of(existingPeriod));

        when(periodRepository.existsOverlappingPeriodExcludingId(CURRENT_YEAR, newStart, newEnd, updatedId))
                .thenReturn(true);

        when(periodRepository.existsByNameAndAcademicYear(newName, CURRENT_YEAR))
                .thenReturn(false);

        assertThatThrownBy(() -> periodService.updatePeriod(updatedId, newName, newStart, newEnd))
                .isInstanceOf(PeriodOverlapException.class);

        verify(periodRepository).findByIdAndAcademicYear(1L, CURRENT_YEAR);
        verify(periodRepository).existsOverlappingPeriodExcludingId(CURRENT_YEAR, newStart, newEnd, updatedId);
    }

    @Test
    void updatePeriod_shouldThrow_whenStartAfterEnd() {
        Long updatedId = 1L;
        String  newName = "1 QUARTER";
        LocalDate newStart = LocalDate.of(2025, 11, 25);
        LocalDate newEnd = LocalDate.of(2025, 10, 29);

        when(periodRepository.findByIdAndAcademicYear(updatedId, CURRENT_YEAR))
                .thenReturn(Optional.of(new Period()));

        assertThatThrownBy(() -> periodService.updatePeriod(updatedId, newName, newStart, newEnd))
                .isInstanceOf(InvalidPeriodDatesException.class);

        verify(periodRepository).findByIdAndAcademicYear(1L, CURRENT_YEAR);
        verify(periodRepository, never()).existsOverlappingPeriodExcludingId(CURRENT_YEAR, newStart, newEnd, updatedId);
    }

    @Test
    void getPeriod_shouldReturnPeriod_whenIdExists() {
        Period existingPeriod = new Period();

        when(periodRepository.findByIdAndAcademicYear(1L, CURRENT_YEAR)).thenReturn(Optional.of(existingPeriod));

        Period result = periodService.getPeriod(1L);

        assertThat(result).isSameAs(existingPeriod);
    }

    @Test
    void getPeriod_shouldThrow_whenNotFound() {
        when(periodRepository.findByIdAndAcademicYear(1L, CURRENT_YEAR))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> periodService.getPeriod(1L))
                .isInstanceOf(PeriodNotFoundException.class);
    }

    @Test
    void getAllPeriods_shouldReturnList_whenPeriodsExist() {
        List<Period> existingPeriods = List.of(new Period(), new Period());

        when(periodRepository.findAllByAcademicYear(CURRENT_YEAR)).thenReturn(existingPeriods);

        List<Period> result = periodService.getAllPeriods();

        assertThat(result).isSameAs(existingPeriods);
        assertThat(result).containsExactlyElementsOf(existingPeriods);
    }

    @Test
    void getAllPeriods_shouldReturnEmptyList_whenNoPeriodsExist() {
        when(periodRepository.findAllByAcademicYear(CURRENT_YEAR))
                .thenReturn(Collections.emptyList());

        List<Period> result = periodService.getAllPeriods();

        assertThat(result).isEmpty();
    }
}
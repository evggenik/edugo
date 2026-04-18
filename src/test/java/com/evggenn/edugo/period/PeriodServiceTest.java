package com.evggenn.edugo.period;

import com.evggenn.edugo.period.exception.InvalidPeriodDatesException;
import com.evggenn.edugo.period.exception.PeriodAlreadyExistsException;
import com.evggenn.edugo.period.exception.PeriodOverlapException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

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

        periodService.createPeriod("1 четверть",  newStart, newEnd, CURRENT_YEAR);

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

        assertThatThrownBy(() -> periodService.createPeriod("1 четверть", newStart, newEnd, CURRENT_YEAR))
                .isInstanceOf(InvalidPeriodDatesException.class);

        verifyNoInteractions(periodRepository);
    }

    @Test
    void createPeriod_shouldThrow_whenNameAlreadyExists() {
        LocalDate newStart = LocalDate.of(2025, 1, 1);
        LocalDate newEnd = LocalDate.of(2025, 10, 31);

        when(periodRepository.existsByNameAndAcademicYear("1 четверть", CURRENT_YEAR)).thenReturn(true);

        assertThatThrownBy(() -> periodService.createPeriod("1 четверть", newStart, newEnd, CURRENT_YEAR))
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


        assertThatThrownBy(() -> periodService.createPeriod("2 четверть", newStart, newEnd, CURRENT_YEAR))
                .isInstanceOf(PeriodOverlapException.class);

        verify(periodRepository).existsByNameAndAcademicYear("2 четверть", CURRENT_YEAR);
        verify(periodRepository).existsOverlappingPeriod(CURRENT_YEAR, newStart, newEnd);
        verify(periodRepository, never()).save(any());
    }
}
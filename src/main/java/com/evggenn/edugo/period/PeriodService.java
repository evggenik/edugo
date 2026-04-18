package com.evggenn.edugo.period;

import com.evggenn.edugo.period.exception.InvalidPeriodDatesException;
import com.evggenn.edugo.period.exception.PeriodAlreadyExistsException;
import com.evggenn.edugo.period.exception.PeriodOverlapException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class PeriodService {

private final PeriodRepository periodRepository;

    public Period createPeriod(
            String name, LocalDate newStart, LocalDate newEnd, String currentYear) {

        if (!newStart.isBefore(newEnd)) {
            throw new InvalidPeriodDatesException(newStart, newEnd);
        }

        if (periodRepository.existsByNameAndAcademicYear(name, currentYear)) {
            throw new PeriodAlreadyExistsException(name, currentYear);
        }

        if (periodRepository.existsOverlappingPeriod(currentYear, newStart, newEnd)) {
            throw new PeriodOverlapException(currentYear);
        }

        Period period = new Period(name, newStart, newEnd, currentYear);

        return periodRepository.save(period);
    }
}

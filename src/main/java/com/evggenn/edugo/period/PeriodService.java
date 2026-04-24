package com.evggenn.edugo.period;

import com.evggenn.edugo.period.exception.InvalidPeriodDatesException;
import com.evggenn.edugo.period.exception.PeriodAlreadyExistsException;
import com.evggenn.edugo.period.exception.PeriodNotFoundException;
import com.evggenn.edugo.period.exception.PeriodOverlapException;
import com.evggenn.edugo.util.AcademicYearUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PeriodService {

private final PeriodRepository periodRepository;

    @Transactional
    public Period createPeriod(
            String name, LocalDate newStart, LocalDate newEnd) {
        String currentYear = AcademicYearUtil.getCurrentAcademicYear();

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

    @Transactional
    public Period updatePeriod(Long updatedId,
                               String newName,
                               LocalDate newStart,
                               LocalDate newEnd) {
        String currentYear = AcademicYearUtil.getCurrentAcademicYear();

        Period period = periodRepository.findByIdAndAcademicYear(updatedId, currentYear)
                .orElseThrow(() -> new PeriodNotFoundException(updatedId, currentYear));

        if (!newStart.isBefore(newEnd)) {
            throw new InvalidPeriodDatesException(newStart, newEnd);
        }

        if (!period.getName().equals(newName) &&
                periodRepository.existsByNameAndAcademicYear(newName, currentYear)) {
            throw new PeriodAlreadyExistsException(newName, currentYear);
        }

        if (periodRepository.existsOverlappingPeriodExcludingId(currentYear, newStart, newEnd,  updatedId)) {
            throw new PeriodOverlapException(currentYear);
        }

        period.setName(newName);
        period.setStartDate(newStart);
        period.setEndDate(newEnd);

        return period;
    }

    @Transactional(readOnly = true)
    public Period getPeriod(Long periodId) {
        String currentYear = AcademicYearUtil.getCurrentAcademicYear();

        return periodRepository.findByIdAndAcademicYear(periodId, currentYear)
                .orElseThrow(() -> new PeriodNotFoundException(periodId, currentYear));
    }

    @Transactional(readOnly = true)
    public List<Period> getAllPeriods() {
        String currentYear = AcademicYearUtil.getCurrentAcademicYear();

        return periodRepository.findAllByAcademicYear(currentYear);
    }
}

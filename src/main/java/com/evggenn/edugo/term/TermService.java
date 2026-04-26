package com.evggenn.edugo.term;

import com.evggenn.edugo.term.exception.InvalidTermDatesException;
import com.evggenn.edugo.term.exception.TermAlreadyExistsException;
import com.evggenn.edugo.term.exception.TermNotFoundException;
import com.evggenn.edugo.term.exception.TermOverlapException;
import com.evggenn.edugo.util.AcademicYearUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TermService {

private final TermRepository termRepository;

    @Transactional
    public Term createPeriod(
            String name, LocalDate newStart, LocalDate newEnd) {
        String currentYear = AcademicYearUtil.getCurrentAcademicYear();

        if (!newStart.isBefore(newEnd)) {
            throw new InvalidTermDatesException(newStart, newEnd);
        }

        if (termRepository.existsByNameAndAcademicYear(name, currentYear)) {
            throw new TermAlreadyExistsException(name, currentYear);
        }

        if (termRepository.existsOverlappingPeriod(currentYear, newStart, newEnd)) {
            throw new TermOverlapException(currentYear);
        }

        Term term = new Term(name, newStart, newEnd, currentYear);

        return termRepository.save(term);
    }

    @Transactional
    public Term updatePeriod(Long updatedId,
                             String newName,
                             LocalDate newStart,
                             LocalDate newEnd) {
        String currentYear = AcademicYearUtil.getCurrentAcademicYear();

        Term term = termRepository.findByIdAndAcademicYear(updatedId, currentYear)
                .orElseThrow(() -> new TermNotFoundException(updatedId, currentYear));

        if (!newStart.isBefore(newEnd)) {
            throw new InvalidTermDatesException(newStart, newEnd);
        }

        if (!term.getName().equals(newName) &&
                termRepository.existsByNameAndAcademicYear(newName, currentYear)) {
            throw new TermAlreadyExistsException(newName, currentYear);
        }

        if (termRepository.existsOverlappingPeriodExcludingId(currentYear, newStart, newEnd,  updatedId)) {
            throw new TermOverlapException(currentYear);
        }

        term.setName(newName);
        term.setStartDate(newStart);
        term.setEndDate(newEnd);

        return term;
    }

    @Transactional(readOnly = true)
    public Term getPeriod(Long periodId) {
        String currentYear = AcademicYearUtil.getCurrentAcademicYear();

        return termRepository.findByIdAndAcademicYear(periodId, currentYear)
                .orElseThrow(() -> new TermNotFoundException(periodId, currentYear));
    }

    @Transactional(readOnly = true)
    public List<Term> getAllPeriods() {
        String currentYear = AcademicYearUtil.getCurrentAcademicYear();

        return termRepository.findAllByAcademicYear(currentYear);
    }
}

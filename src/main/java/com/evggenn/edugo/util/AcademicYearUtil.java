package com.evggenn.edugo.util;

import java.time.LocalDate;

public class AcademicYearUtil {
    public static String getCurrentAcademicYear() {
        LocalDate now = LocalDate.now();
        int startYear = (now.getMonthValue() >= 9) ? now.getYear() : now.getYear() - 1;
        return String.format("%04d-%04d", startYear, startYear + 1);
    }
}

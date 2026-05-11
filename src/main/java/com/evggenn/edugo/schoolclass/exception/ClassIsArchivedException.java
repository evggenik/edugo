package com.evggenn.edugo.schoolclass.exception;

public class ClassIsArchivedException extends RuntimeException {
    public ClassIsArchivedException(String academicYear) {
        super(String.format("The school class can not be modified: it is archived for year %s", academicYear));
    }
}

package com.evggenn.edugo.lesson.exception;

import java.time.LocalDateTime;

public class InvalidTimesException extends RuntimeException {
    public InvalidTimesException(LocalDateTime startTime, LocalDateTime endTime) {
        super(String.format("Start date: %s can not be after end date: %s", startTime, endTime));
    }
}
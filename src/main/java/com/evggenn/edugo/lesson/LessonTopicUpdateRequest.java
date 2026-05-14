package com.evggenn.edugo.lesson;

import jakarta.validation.constraints.Size;

public record LessonTopicUpdateRequest(
        String topic,
        @Size(max = 20) String room
) {}

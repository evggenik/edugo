package com.evggenn.edugo.attendance;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/atendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @GetMapping("/lesson/{lessonId}")
    public ResponseEntity<List<AttendanceResponse>> findAllByLessonId(
            @PathVariable Long lessonId) {

        List<Attendance> attendances = attendanceService.getAttendanceByLesson(lessonId);

        return ResponseEntity.ok(attendances.stream()
                .map(AttendanceResponse::from).toList());
    }
}

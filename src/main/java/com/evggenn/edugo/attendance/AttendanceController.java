package com.evggenn.edugo.attendance;

import com.evggenn.edugo.user.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/atendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping
    public ResponseEntity<AttendanceResponse> createAttendance(
            @Valid @RequestBody AttendanceRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        Attendance attendance = attendanceService.createAttendance(
                request.status(),
                request.studentId(),
                request.lessonId(),
                currentUser.getId());

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(attendance.getId())
                .toUri();

        return ResponseEntity.created(location)
                .body(AttendanceResponse.from(attendance));
    }

    @GetMapping("/lesson/{lessonId}")
    public ResponseEntity<List<AttendanceResponse>> findAllByLessonId(
            @PathVariable Long lessonId) {

        List<Attendance> attendances = attendanceService.getAttendanceByLesson(lessonId);

        return ResponseEntity.ok(attendances.stream()
                .map(AttendanceResponse::from).toList());
    }
}

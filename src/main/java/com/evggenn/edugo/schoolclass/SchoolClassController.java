package com.evggenn.edugo.schoolclass;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/classes")
@RequiredArgsConstructor
public class SchoolClassController {

    private final SchoolClassService classService;

    @PostMapping
    public ResponseEntity<SchoolClassResponse> createClass(@Valid @RequestBody SchoolClassCreateRequest request) {
        SchoolClassResponse response = classService.createClass(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{classId}")
    public ResponseEntity<SchoolClassResponse> updateClass(
            @PathVariable Long classId,
            @Valid @RequestBody SchoolClassUpdateRequest request
    ) {
        SchoolClassResponse response = classService.updateClass(
                classId,
                request
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{classId}/students/{studentId}")
    public ResponseEntity<SchoolClassResponse> addStudentToClass(
            @PathVariable Long classId,
            @PathVariable Long studentId) {
        SchoolClassResponse response = classService.addStudentToClass(classId, studentId);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{classId}/students/{studentId}")
    public ResponseEntity<SchoolClassResponse> removeStudentFromClass(
            @PathVariable Long classId,
            @PathVariable Long studentId) {
        SchoolClassResponse response = classService.removeStudentFromClass(classId, studentId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{classId}")
    public ResponseEntity<SchoolClassResponse> getClass(
            @PathVariable Long classId) {

        SchoolClassResponse response = classService.getSchoolClass(classId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<SchoolClassResponse>> getAllClasses(
            @RequestParam("academicYear") String academicYear) {
        return ResponseEntity.ok(classService.getAllClasses(academicYear));
    }
}

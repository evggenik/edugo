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
    private final SchoolClassMapper mapper;

    @PostMapping
    public ResponseEntity<SchoolClassResponse> createClass(@Valid @RequestBody SchoolClassRequest request) {
        SchoolClass schoolClass = classService.createClass(request.name(), request.academicYear());

        SchoolClassResponse response = mapper.toResponse(schoolClass);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{classId}")
    public ResponseEntity<SchoolClassResponse> updateClass(
            @PathVariable Long classId,
            @Valid @RequestBody SchoolClassRequest request
    ) {
        SchoolClass schoolClass = classService.updateClass(
                classId,
                request.name(),
                request.academicYear(),
                request.teacherId()
        );

        SchoolClassResponse response = mapper.toResponse(schoolClass);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{classId}/students/{studentId}")
    public ResponseEntity<SchoolClassResponse> addStudentToClass(
            @PathVariable Long classId,
            @PathVariable Long studentId) {
        SchoolClass schoolClass = classService.addStudentToClass(classId, studentId);
        return ResponseEntity.ok(mapper.toResponse(schoolClass));
    }

    @DeleteMapping("/{classId}/students/{studentId}")
    public ResponseEntity<SchoolClassResponse> removeStudentFromClass(
            @PathVariable Long classId,
            @PathVariable Long studentId) {
        SchoolClass schoolClass = classService.removeStudentFromClass(classId, studentId);
        return ResponseEntity.ok(mapper.toResponse(schoolClass));
    }

    @GetMapping("/{classId}")
    public ResponseEntity<SchoolClassResponse> getClass(
            @PathVariable Long classId) {

        SchoolClass schoolClass = classService.getSchoolClass(classId);
        return ResponseEntity.ok(mapper.toResponse(schoolClass));
    }

    @GetMapping
    public ResponseEntity<List<SchoolClassResponse>> getAllClasses(
            @RequestParam("academicYear") String academicYear) {

        List<SchoolClass> schoolClassList = classService.getAllClasses(academicYear);
        return ResponseEntity.ok(schoolClassList.stream()
                .map(mapper::toResponse)
                .toList());
    }
}

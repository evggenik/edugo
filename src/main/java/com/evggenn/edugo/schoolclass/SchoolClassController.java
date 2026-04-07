package com.evggenn.edugo.schoolclass;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}

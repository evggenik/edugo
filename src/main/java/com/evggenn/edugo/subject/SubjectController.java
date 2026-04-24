package com.evggenn.edugo.subject;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/subjects")
public class SubjectController {

    private final SubjectService subjectService;

    @PostMapping
    public ResponseEntity<Subject> createSubject(@Valid @RequestBody SubjectRequest request) {
        Subject subject = subjectService.createSubject(request.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(subject);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Subject> updateSubject(
            @PathVariable Long id,
            @Valid @RequestBody SubjectRequest subjectRequest
    ) {
        Subject subject = subjectService.updateSubject(id, subjectRequest.getName());
        return ResponseEntity.status(HttpStatus.OK).body(subject);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Subject> getSubject(@PathVariable Long id) {
        Subject subject = subjectService.getSubject(id);
        return ResponseEntity.status(HttpStatus.OK).body(subject);
    }

    @GetMapping
    public ResponseEntity<List<Subject>> getAllSubjects() {
        List<Subject> subjectList = subjectService.getAllSubjects();
        return ResponseEntity.status(HttpStatus.OK).body(subjectList);
    }
}

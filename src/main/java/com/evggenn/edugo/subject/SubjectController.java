package com.evggenn.edugo.subject;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/subjects")
public class SubjectController {

    private final SubjectService subjectService;

    @PostMapping
    public ResponseEntity<SubjectResponse> createSubject(@Valid @RequestBody SubjectRequest request) {
        Subject subject = subjectService.createSubject(request.getName());

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(subject.getId())
                .toUri();

        return ResponseEntity.created(location).body(SubjectResponse.from(subject));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<SubjectResponse> updateSubject(
            @PathVariable Long id,
            @Valid @RequestBody SubjectRequest subjectRequest
    ) {

        Subject subject = subjectService.updateSubject(id, subjectRequest.getName());

        return ResponseEntity.ok(SubjectResponse.from(subject));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubjectResponse> getSubject(@PathVariable Long id) {

        Subject subject = subjectService.getSubject(id);

        return ResponseEntity.status(HttpStatus.OK).body(SubjectResponse.from(subject));
    }

    @GetMapping
    public ResponseEntity<List<SubjectResponse>> getAllSubjects() {

        List<Subject> subjectList = subjectService.getAllSubjects();

        List<SubjectResponse> respList = subjectList.stream()
                .map(SubjectResponse::from).toList();

        return ResponseEntity.status(HttpStatus.OK).body(respList);
    }
}

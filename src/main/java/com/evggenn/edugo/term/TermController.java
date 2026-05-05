package com.evggenn.edugo.term;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/periods")
@RequiredArgsConstructor
public class TermController {

    private final TermService termService;

    @PostMapping
    public ResponseEntity<TermResponse> createPeriod(@Valid @RequestBody CreateRequest request) {
        Term term = termService.createPeriod(
                request.name(),
                request.startDate(),
                request.endDate()
        );

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(term.getId())
                .toUri();

        return ResponseEntity.created(location).body(toResponse(term));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TermResponse> updatePeriod(@PathVariable Long id,
                                                     @Valid @RequestBody UpdateRequest request) {

        Term term = termService.updatePeriod(
                id,
                request.name(),
                request.startDate(),
                request.endDate()
        );

        return ResponseEntity.ok(toResponse(term));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TermResponse> getPeriod(@PathVariable Long id) {

        return ResponseEntity.ok(toResponse(termService.getPeriod(id)));
    }

    @GetMapping
    public ResponseEntity<List<TermResponse>> getAllPeriods() {

        List<TermResponse> list = termService.getAllPeriods().stream()
                .map(this::toResponse).toList();

        return ResponseEntity.ok(list);
    }

    private TermResponse toResponse(Term p) {
        return new TermResponse(
                p.getId(),
                p.getName(),
                p.getStartDate(),
                p.getEndDate()
        );
    }
}

package com.evggenn.edugo.period;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/periods")
@RequiredArgsConstructor
public class PeriodController {

    private final PeriodService periodService;

    @PostMapping
    public ResponseEntity<PeriodResponse> createPeriod(@Valid @RequestBody CreateRequest request) {
        Period period = periodService.createPeriod(
                request.name(),
                request.startDate(),
                request.endDate()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(period));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<PeriodResponse> updatePeriod(@PathVariable Long id,
                                               @Valid @RequestBody UpdateRequest request) {

        Period period = periodService.updatePeriod(
                id,
                request.name(),
                request.startDate(),
                request.endDate()
        );

        return ResponseEntity.ok(toResponse(period));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PeriodResponse> getPeriod(@PathVariable Long id) {

        return ResponseEntity.ok(toResponse(periodService.getPeriod(id)));
    }

    @GetMapping
    public ResponseEntity<List<PeriodResponse>> getAllPeriods() {

        List<PeriodResponse> list = periodService.getAllPeriods().stream()
                .map(this::toResponse).toList();

        return ResponseEntity.ok(list);
    }

    private PeriodResponse toResponse(Period p) {
        return new PeriodResponse(
                p.getId(),
                p.getName(),
                p.getStartDate(),
                p.getEndDate()
        );
    }
}

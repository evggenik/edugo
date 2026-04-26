package com.evggenn.edugo.term;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "periods",
        uniqueConstraints = @UniqueConstraint(
                name = "uniq_periods_name_academic_year",
                columnNames = {"name", "academic_year"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Setter
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Term {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    /* equals/hashCode based on id only.
    Transient instances (id = null) are all "equal" —
    avoid using in Sets before persist.
    */
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "academic_year", nullable = false, length = 9)
    private String academicYear;

    public Term(String name,
                LocalDate startDate,
                LocalDate endDate,
                String academicYear) {
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.academicYear = academicYear;
    }
}

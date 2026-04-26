package com.evggenn.edugo.subject;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "subjects")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Setter
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Subject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    /* equals/hashCode based on id only.
    Transient instances (id = null) are all "equal" —
    avoid using in Sets before persist.
    */
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    public Subject(String name) {
        this.name = name;
    }
}

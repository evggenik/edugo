package com.evggenn.edugo.grade;

import com.evggenn.edugo.lesson.Lesson;
import com.evggenn.edugo.subject.Subject;
import com.evggenn.edugo.term.Term;
import com.evggenn.edugo.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.time.Instant;

@Table(name="grades")
@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter @Setter
@Builder
@ToString(exclude = {"student", "lesson", "term"})
public class Grade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "value", nullable = false)
    @Min(2) @Max(5)
    private short value;

    @Column(name = "type", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private GradeType type;

    @Column(name = "comment",  length = 500)
    private String comment;

    @Column(name = "graded_at", nullable = false)
    private Instant gradedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id")
    private Lesson lesson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "period_id")
    private Term term;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    private Subject subject;

    @PrePersist
    protected void onCreate() {
        if (gradedAt == null) gradedAt = Instant.now();
    }
}

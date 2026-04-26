package com.evggenn.edugo.lesson;


import com.evggenn.edugo.term.Term;
import com.evggenn.edugo.schoolclass.SchoolClass;
import com.evggenn.edugo.subject.Subject;
import com.evggenn.edugo.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Setter
@Getter
@Table(name = "lessons")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"schoolClass", "subject", "teacher", "term"})
public class Lesson {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    /* equals/hashCode based on id only.
    Transient instances (id = null) are all "equal" —
    avoid using in Sets before persist.
    */
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "topic", length = 255)
    private String topic;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "room", length = 20)
    private String room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    private SchoolClass schoolClass;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id",  nullable = false)
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id",  nullable = false)
    private User teacher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "period_id", nullable = false)
    private Term term;
}

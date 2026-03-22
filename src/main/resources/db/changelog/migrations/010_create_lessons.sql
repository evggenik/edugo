CREATE TABLE lessons(
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    topic VARCHAR(255),
    class_id BIGINT NOT NULL,
    subject_id BIGINT NOT NULL,
    teacher_id BIGINT NOT NULL,
    period_id BIGINT NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    room VARCHAR(20),
    FOREIGN KEY (class_id) REFERENCES classes(id),
    FOREIGN KEY (subject_id) REFERENCES subjects(id),
    FOREIGN KEY (teacher_id) REFERENCES users(id),
    FOREIGN KEY (period_id) REFERENCES periods(id),
    CONSTRAINT chk_lessons_times CHECK ( start_time < end_time ),
    CONSTRAINT chk_lessons_dates CHECK ( start_time::date = end_time::date )
);
CREATE TABLE student_classes(
    student_id BIGINT NOT NULL,
    class_id BIGINT NOT NULL,
    PRIMARY KEY (student_id, class_id),
    FOREIGN KEY (student_id) REFERENCES users(id),
    FOREIGN KEY (class_id) REFERENCES classes(id)
);
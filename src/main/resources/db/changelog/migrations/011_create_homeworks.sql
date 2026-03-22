CREATE TABLE homeworks(
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  assigned_lesson_id BIGINT NOT NULL,
  description TEXT NOT NULL,
  due_date DATE NOT NULL,
  FOREIGN KEY (assigned_lesson_id) REFERENCES lessons(id) ON DELETE CASCADE
);

CREATE INDEX idx_homeworks_assigned_lesson_id ON homeworks(assigned_lesson_id);
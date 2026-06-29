ALTER TABLE homeworks
ADD CONSTRAINT uq_homeworks_assigned_lesson_id UNIQUE (assigned_lesson_id);
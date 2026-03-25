INSERT INTO roles (name) VALUES
                             ('STUDENT'),
                             ('TEACHER'),
                             ('PARENT'),
                             ('ADMIN')
ON CONFLICT (name) DO NOTHING;
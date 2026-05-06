WITH users_ins AS (
    INSERT INTO users (email,
                       password,
                       first_name,
                       last_name,
                       created_at)
    VALUES ('admin@admin.com',
            '$2a$10$hzIrv4sJWWYLN/LZlGM44u77ZQhHecahD5DSOEFEKeIhhqSqIav..',
            'Джон',
            'Смит',
            now()
           )
    ON CONFLICT (email) DO NOTHING
    RETURNING id
)

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users_ins u, roles r
WHERE r.name = 'ADMIN';




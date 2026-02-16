INSERT INTO users (id, name, email)
VALUES (1, 'Seed User', 'seed-user-1@ewm.local')
ON CONFLICT DO NOTHING;

INSERT INTO categories (id, name)
VALUES (1, 'Seed Category')
ON CONFLICT DO NOTHING;

INSERT INTO events (id, title, annotation, description, event_date, initiator_id, category_id, paid,
                    participant_limit, request_moderation, status, created_on, published_on, confirmed_requests)
VALUES (1, 'Seed Event', 'Seed annotation', 'Seed description', '2026-03-01 12:00:00',
        1, 1, FALSE, 0, TRUE, 'PUBLISHED', '2026-02-10 12:00:00', '2026-02-10 12:30:00', 0)
ON CONFLICT DO NOTHING;

INSERT INTO comments (id, author_id, event_id, text, created_on, moderation_status)
VALUES (1, 1, 1, 'Seed approved comment', '2026-02-10 13:00:00', 'APPROVED')
ON CONFLICT DO NOTHING;

SELECT setval(pg_get_serial_sequence('users', 'id'), GREATEST((SELECT COALESCE(MAX(id), 1) FROM users), 1));
SELECT setval(pg_get_serial_sequence('categories', 'id'), GREATEST((SELECT COALESCE(MAX(id), 1) FROM categories), 1));
SELECT setval(pg_get_serial_sequence('events', 'id'), GREATEST((SELECT COALESCE(MAX(id), 1) FROM events), 1));
SELECT setval(pg_get_serial_sequence('comments', 'id'), GREATEST((SELECT COALESCE(MAX(id), 1) FROM comments), 1));

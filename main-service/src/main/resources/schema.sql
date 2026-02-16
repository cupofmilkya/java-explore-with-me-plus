-- Таблица пользователей
CREATE TABLE IF NOT EXISTS users
(
    id    BIGSERIAL PRIMARY KEY,
    name  TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE
);

-- Таблица категорий
CREATE TABLE IF NOT EXISTS categories
(
    id   BIGSERIAL PRIMARY KEY,
    name TEXT NOT NULL UNIQUE
);

-- Таблица событий
CREATE TABLE IF NOT EXISTS events
(
    id                 BIGSERIAL PRIMARY KEY,
    title              TEXT,
    annotation         TEXT,
    description        TEXT,
    event_date         TIMESTAMP,
    initiator_id       BIGINT REFERENCES users (id),
    category_id        BIGINT REFERENCES categories (id),
    lat                DOUBLE PRECISION,
    lon                DOUBLE PRECISION,
    paid               BOOLEAN,
    participant_limit  INT,
    request_moderation BOOLEAN,
    status             TEXT CHECK (status IN ('PENDING', 'PUBLISHED', 'CANCELED')),
    created_on         TIMESTAMP,
    published_on       TIMESTAMP,
    confirmed_requests BIGINT DEFAULT 0
);

-- Таблица комментариев
CREATE TABLE IF NOT EXISTS comments
(
    id                BIGSERIAL PRIMARY KEY,
    author_id         BIGINT REFERENCES users (id),
    event_id          BIGINT REFERENCES events (id),
    text              TEXT NOT NULL,
    created_on        TIMESTAMP NOT NULL,
    moderation_status TEXT CHECK (moderation_status IN ('PENDING', 'APPROVED', 'REJECTED'))
);

-- Таблица подборок (compilations)
CREATE TABLE IF NOT EXISTS compilations
(
    id     BIGSERIAL PRIMARY KEY,
    title  TEXT NOT NULL,
    pinned BOOLEAN DEFAULT FALSE
);

-- Связующая таблица для compilation -> events
CREATE TABLE IF NOT EXISTS compilation_events
(
    compilation_id BIGINT REFERENCES compilations (id) ON DELETE CASCADE,
    event_id       BIGINT REFERENCES events (id) ON DELETE CASCADE,
    PRIMARY KEY (compilation_id, event_id)
);

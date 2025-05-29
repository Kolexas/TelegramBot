-- liquibase formatted sql

-- changeset Kolexas:1
CREATE TABLE NotificationTask (
    id SERIAL PRIMARY KEY,
    chat_id BIGINT NOT NULL,
    message Text,
    date TIMESTAMP
)
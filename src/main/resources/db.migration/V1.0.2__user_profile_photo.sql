CREATE TABLE user_photos (
    id CHAR(36) NOT NULL,
    active BOOLEAN NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    deleted_at DATETIME,
    user_id CHAR(36) NOT NULL,
    content MEDIUMBLOB NOT NULL,
    content_type VARCHAR(50) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_user_photos_user UNIQUE (user_id),
    CONSTRAINT fk_user_photos_user FOREIGN KEY (user_id) REFERENCES users(id)
);

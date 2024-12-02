CREATE TABLE documents
(
    id            CHAR(36) PRIMARY KEY,
    user_id       CHAR(36)     NOT NULL,
    document_type VARCHAR(255) NOT NULL,
    file_name     VARCHAR(255),
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id)
);
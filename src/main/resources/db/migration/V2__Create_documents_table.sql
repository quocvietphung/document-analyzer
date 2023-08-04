CREATE TABLE documents
(
    id SERIAL PRIMARY KEY,
    user_id CHAR(36) NOT NULL,
    document_type VARCHAR(255),
    document TEXT,
    FOREIGN KEY (user_id) REFERENCES users (id)
);

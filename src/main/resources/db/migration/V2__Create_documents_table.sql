CREATE TABLE documents
(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id CHAR(36) NOT NULL,
    document_type VARCHAR(255),
    file_name VARCHAR(255),
    file_path VARCHAR(255),
    FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE selbstauskunft
(
    id                 CHAR(36)    NOT NULL PRIMARY KEY,
    user_id            CHAR(36)    NOT NULL UNIQUE,
    document_id        CHAR(36) UNIQUE,
    number_of_children INT         NOT NULL,
    status             VARCHAR(20) NOT NULL,
    application_date   DATE,
    created_at         TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (document_id) REFERENCES documents (id) ON DELETE SET NULL ON UPDATE CASCADE
);
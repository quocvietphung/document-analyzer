CREATE TABLE credit_requests
(
    id                    CHAR(36)       NOT NULL PRIMARY KEY,
    user_id               CHAR(36)       NOT NULL,
    kredit_typ            VARCHAR(255)   NOT NULL,
    kredit_link           VARCHAR(512),
    betrag                DECIMAL(15, 2) NOT NULL,
    laufzeit              INTEGER        NOT NULL,
    created_at            TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id)
);
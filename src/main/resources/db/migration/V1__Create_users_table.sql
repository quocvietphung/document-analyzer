CREATE TABLE users
(
    id                            CHAR(36)      NOT NULL PRIMARY KEY,
    first_name                    VARCHAR(255)  NOT NULL,
    last_name                     VARCHAR(255)  NOT NULL,
    phone_number                  VARCHAR(255)  NOT NULL,
    email                         VARCHAR(255)  NOT NULL UNIQUE,
    password                      VARCHAR(255)  NOT NULL UNIQUE,
    role                          VARCHAR(50)   NOT NULL,
    is_active                     TINYINT(1)    NOT NULL,
    document_upload_percentage    DECIMAL(5, 1) NOT NULL,
    terms_and_conditions_accepted TINYINT(1)    NOT NULL,
    privacy_policy_accepted       TINYINT(1)    NOT NULL,
    usage_terms_accepted          TINYINT(1)    NOT NULL,
    consent_terms_accepted        TINYINT(1)    NOT NULL,
    created_at                    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
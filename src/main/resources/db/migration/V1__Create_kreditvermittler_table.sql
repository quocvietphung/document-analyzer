CREATE TABLE kreditvermittler
(
    id                            CHAR(36)     NOT NULL PRIMARY KEY,
    first_name                    VARCHAR(255) NOT NULL,
    last_name                     VARCHAR(255) NOT NULL,
    phone_number                  VARCHAR(255) NOT NULL,
    email                         VARCHAR(255) NOT NULL UNIQUE,
    role                          VARCHAR(255) NOT NULL,
    terms_and_conditions_accepted TINYINT(1)   NOT NULL,
    privacy_policy_accepted       TINYINT(1)   NOT NULL,
    usage_terms_accepted          TINYINT(1)   NOT NULL,
    consent_terms_accepted        TINYINT(1)   NOT NULL,
    logo                          VARCHAR(255),
    profile_image                 VARCHAR(255),
    created_at                    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at                    TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

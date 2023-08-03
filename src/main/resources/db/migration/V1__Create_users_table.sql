CREATE TABLE users
(
    id                            CHAR(36)     NOT NULL PRIMARY KEY,
    company_name                  VARCHAR(255),
    first_name                    VARCHAR(255) NOT NULL,
    last_name                     VARCHAR(255) NOT NULL,
    phone_number                  VARCHAR(255) NOT NULL,
    email                         VARCHAR(255) NOT NULL,
    username                      VARCHAR(255) NOT NULL,
    password                      VARCHAR(255) NOT NULL,
    role                          VARCHAR(255) NOT NULL,
    privacy_policy_accepted       TINYINT(1)   NOT NULL,
    terms_and_conditions_accepted TINYINT(1)   NOT NULL,
    created_at                    TIMESTAMP    NOT NULL,
    updated_at                    TIMESTAMP    NOT NULL
);

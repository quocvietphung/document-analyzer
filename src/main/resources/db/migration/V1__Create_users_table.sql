CREATE TABLE users
(
    id                         CHAR(36)     NOT NULL PRIMARY KEY,
    companyName                VARCHAR(255),
    firstName                  VARCHAR(255) NOT NULL,
    lastName                   VARCHAR(255) NOT NULL,
    phoneNumber                VARCHAR(255) NOT NULL,
    email                      VARCHAR(255) NOT NULL,
    username                   VARCHAR(255) NOT NULL,
    password                   VARCHAR(255) NOT NULL,
    role                       VARCHAR(255) NOT NULL,
    privacyPolicyAccepted      TINYINT(1)   NOT NULL,
    termsAndConditionsAccepted TINYINT(1)   NOT NULL,
    createdAt                  TIMESTAMP    NOT NULL,
    updatedAt                  TIMESTAMP    NOT NULL
);

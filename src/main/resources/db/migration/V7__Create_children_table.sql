CREATE TABLE children
(
    id                CHAR(36)     NOT NULL PRIMARY KEY,
    selbstauskunft_id CHAR(36)     NOT NULL,
    name              VARCHAR(255) NOT NULL,
    date_of_birth     DATE         NOT NULL,
    child_benefit     TINYINT(1)   NOT NULL,
    alimony_payments  TINYINT(1)   NOT NULL,
    monthly_amount    DECIMAL(15, 2),
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (selbstauskunft_id) REFERENCES selbstauskunft (id)
);

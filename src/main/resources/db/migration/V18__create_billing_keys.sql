CREATE TABLE billing_keys
(
    id                 BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id            BIGINT       NOT NULL,
    billing_key        VARCHAR(200) NOT NULL,
    customer_key       VARCHAR(300) NOT NULL,
    mid                VARCHAR(14),
    method             VARCHAR(20),
    authenticated_at   DATETIME,
    card_issuer_code   VARCHAR(2),
    card_acquirer_code VARCHAR(2),
    card_number        VARCHAR(20),
    card_type          VARCHAR(10),
    card_owner_type    VARCHAR(10),
    created_at         DATETIME     NOT NULL,
    CONSTRAINT fk_billing_keys_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE RESTRICT
);
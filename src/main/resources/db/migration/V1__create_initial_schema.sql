ALTER DATABASE
    CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE user_company
(
    user_id                 BIGINT       NOT NULL AUTO_INCREMENT,
    sns_id                  VARCHAR(100) NOT NULL,
    oauth_provider          VARCHAR(20)  NOT NULL,
    company_name            VARCHAR(100) NULL,
    business_number         CHAR(12)     NULL,
    business_type           VARCHAR(100) NULL,
    phone_number            CHAR(11)     NULL,
    monthly_expense_budget  INT          NULL,
    receipt_start_date      DATE         NULL,
    last_login_region       VARCHAR(100) NULL,
    user_status             VARCHAR(50)  NOT NULL,
    PRIMARY KEY (user_id),
    CONSTRAINT uk_user_company_oauth_provider_sns_id
        UNIQUE (oauth_provider, sns_id)
) DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE login_history
(
    login_history_id BIGINT       NOT NULL AUTO_INCREMENT,
    user_id          BIGINT       NOT NULL,
    ip_address       VARCHAR(45)  NOT NULL,
    login_region     VARCHAR(100) NULL,
    login_at         DATETIME     NOT NULL,
    PRIMARY KEY (login_history_id),
    CONSTRAINT fk_login_history_user_company
        FOREIGN KEY (user_id) REFERENCES user_company (user_id)
) DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE ocr_option
(
    ocr_option_id               BIGINT  NOT NULL AUTO_INCREMENT,
    user_id                     BIGINT  NOT NULL,
    auto_classification_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    ocr_confidence_threshold    INT     NOT NULL DEFAULT 80,
    PRIMARY KEY (ocr_option_id),
    CONSTRAINT uk_ocr_option_user_id
        UNIQUE (user_id),
    CONSTRAINT fk_ocr_option_user_company
        FOREIGN KEY (user_id) REFERENCES user_company (user_id),
    CONSTRAINT ck_ocr_option_auto_classification_enabled
        CHECK (auto_classification_enabled IN (TRUE, FALSE)),
    CONSTRAINT ck_ocr_option_ocr_confidence_threshold
        CHECK (ocr_confidence_threshold BETWEEN 0 AND 100)
) DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE document_type
(
    document_type_id   BIGINT       NOT NULL AUTO_INCREMENT,
    document_type_name VARCHAR(100) NOT NULL,
    PRIMARY KEY (document_type_id),
    CONSTRAINT uk_document_type_name
        UNIQUE (document_type_name)
) DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE ocr_option_document_type
(
    ocr_option_document_type_id BIGINT NOT NULL AUTO_INCREMENT,
    ocr_option_id               BIGINT NOT NULL,
    document_type_id            BIGINT NOT NULL,
    PRIMARY KEY (ocr_option_document_type_id),
    CONSTRAINT uk_ocr_option_document_type
        UNIQUE (ocr_option_id, document_type_id),
    CONSTRAINT fk_ocr_option_document_type_ocr_option
        FOREIGN KEY (ocr_option_id) REFERENCES ocr_option (ocr_option_id),
    CONSTRAINT fk_ocr_option_document_type_document_type
        FOREIGN KEY (document_type_id) REFERENCES document_type (document_type_id)
) DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE category
(
    category_id   BIGINT       NOT NULL AUTO_INCREMENT,
    user_id       BIGINT       NOT NULL,
    category_name VARCHAR(100) NOT NULL,
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    PRIMARY KEY (category_id),
    CONSTRAINT uk_category_user_id_category_name
        UNIQUE (user_id, category_name),
    CONSTRAINT fk_category_user_company
        FOREIGN KEY (user_id) REFERENCES user_company (user_id)
) DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE receipt
(
    receipt_id                      BIGINT       NOT NULL AUTO_INCREMENT,
    user_id                         BIGINT       NOT NULL,
    category_id                     BIGINT       NULL,
    transaction_at                  DATETIME     NULL,
    merchant_name                   VARCHAR(200) NULL,
    merchant_business_number        VARCHAR(20)  NULL,
    document_type_id                BIGINT       NULL,
    supply_amount                   BIGINT       NULL,
    vat_amount                      BIGINT       NULL,
    total_amount                    BIGINT       NULL,
    payment_method                  VARCHAR(30)  NULL,
    approval_number                 VARCHAR(100) NULL,
    image_url                       VARCHAR(500) NULL,
    original_file_hash              CHAR(64)     NULL,
    review_status                   VARCHAR(20)  NOT NULL,
    registration_type              VARCHAR(20)  NOT NULL,
    ocr_confidence                  INT          NULL,
    recommended_category_name       VARCHAR(100) NULL,
    category_confidence             INT          NULL,
    duplicate_suspicion_score       INT          NOT NULL DEFAULT 0,
    company_relevance_risk_score    INT          NULL,
    memo                            TEXT         NULL,
    created_at                      DATETIME     NOT NULL,
    finalized_at                    DATETIME     NULL,
    deleted_at                      DATETIME     NULL,
    PRIMARY KEY (receipt_id),
    CONSTRAINT uk_receipt_user_id_original_file_hash
        UNIQUE (user_id, original_file_hash),
    CONSTRAINT fk_receipt_user_company
        FOREIGN KEY (user_id) REFERENCES user_company (user_id),
    CONSTRAINT fk_receipt_category
        FOREIGN KEY (category_id) REFERENCES category (category_id),
    CONSTRAINT fk_receipt_document_type
        FOREIGN KEY (document_type_id) REFERENCES document_type (document_type_id),
    CONSTRAINT ck_receipt_review_status
        CHECK (review_status IN ('CONFIRMED', 'REVIEW_REQUIRED', 'UNCLASSIFIED', 'DUPLICATE', 'EXCLUDED')),
    CONSTRAINT ck_receipt_registration_type
        CHECK (registration_type IN ('OCR', 'MANUAL')),
    CONSTRAINT ck_receipt_ocr_confidence
        CHECK (ocr_confidence IS NULL OR ocr_confidence BETWEEN 0 AND 100),
    CONSTRAINT ck_receipt_category_confidence
        CHECK (category_confidence IS NULL OR category_confidence BETWEEN 0 AND 100),
    CONSTRAINT ck_receipt_duplicate_suspicion_score
        CHECK (duplicate_suspicion_score BETWEEN 0 AND 100),
    CONSTRAINT ck_receipt_company_relevance_risk_score
        CHECK (company_relevance_risk_score IS NULL OR company_relevance_risk_score BETWEEN 0 AND 100)
) DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE receipt_item
(
    receipt_item_id BIGINT       NOT NULL AUTO_INCREMENT,
    receipt_id      BIGINT       NOT NULL,
    item_name       VARCHAR(200) NOT NULL,
    quantity        INT          NULL,
    item_amount     INT          NULL,
    PRIMARY KEY (receipt_item_id),
    CONSTRAINT fk_receipt_item_receipt
        FOREIGN KEY (receipt_id) REFERENCES receipt (receipt_id)
) DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE receipt_review_reason
(
    receipt_review_reason_id BIGINT      NOT NULL AUTO_INCREMENT,
    receipt_id               BIGINT      NOT NULL,
    reason_type              VARCHAR(50) NOT NULL,
    reason_message           TEXT        NOT NULL,
    is_resolved              BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at               DATETIME    NOT NULL,
    PRIMARY KEY (receipt_review_reason_id),
    CONSTRAINT uk_receipt_review_reason_receipt_id_reason_type
        UNIQUE (receipt_id, reason_type),
    CONSTRAINT fk_receipt_review_reason_receipt
        FOREIGN KEY (receipt_id) REFERENCES receipt (receipt_id)
) DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE receipt_review_field
(
    receipt_review_reason_id BIGINT      NOT NULL,
    field_name               VARCHAR(50) NOT NULL,
    PRIMARY KEY (receipt_review_reason_id, field_name),
    CONSTRAINT fk_receipt_review_field_receipt_review_reason
        FOREIGN KEY (receipt_review_reason_id)
            REFERENCES receipt_review_reason (receipt_review_reason_id)
) DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE receipt_duplicate_review
(
    receipt_review_reason_id BIGINT NOT NULL,
    related_receipt_id       BIGINT NOT NULL,
    PRIMARY KEY (receipt_review_reason_id),
    CONSTRAINT fk_receipt_duplicate_review_receipt_review_reason
        FOREIGN KEY (receipt_review_reason_id)
            REFERENCES receipt_review_reason (receipt_review_reason_id),
    CONSTRAINT fk_receipt_duplicate_review_related_receipt
        FOREIGN KEY (related_receipt_id) REFERENCES receipt (receipt_id)
) DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE receipt_change_log
(
    receipt_change_log_id BIGINT       NOT NULL AUTO_INCREMENT,
    receipt_id            BIGINT       NOT NULL,
    field_name            VARCHAR(50)  NOT NULL,
    before_value          VARCHAR(500) NULL,
    after_value           VARCHAR(500) NOT NULL,
    created_at            DATETIME     NOT NULL,
    PRIMARY KEY (receipt_change_log_id),
    CONSTRAINT fk_receipt_change_log_receipt
        FOREIGN KEY (receipt_id) REFERENCES receipt (receipt_id)
) DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE category_rule
(
    category_rule_id BIGINT       NOT NULL AUTO_INCREMENT,
    user_id          BIGINT       NOT NULL,
    merchant_name    VARCHAR(200) NOT NULL,
    category_id      BIGINT       NULL,
    created_at       DATETIME     NOT NULL,
    updated_at       DATETIME     NOT NULL,
    PRIMARY KEY (category_rule_id),
    CONSTRAINT uk_category_rule_user_id_merchant_name
        UNIQUE (user_id, merchant_name),
    CONSTRAINT fk_category_rule_user_company
        FOREIGN KEY (user_id) REFERENCES user_company (user_id),
    CONSTRAINT fk_category_rule_category
        FOREIGN KEY (category_id) REFERENCES category (category_id)
) DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE workspace_activity_log
(
    activity_log_id BIGINT       NOT NULL AUTO_INCREMENT,
    user_id         BIGINT       NOT NULL,
    activity_type   VARCHAR(50)  NOT NULL,
    activity_message VARCHAR(255) NOT NULL,
    created_at      DATETIME     NOT NULL,
    PRIMARY KEY (activity_log_id),
    CONSTRAINT fk_workspace_activity_log_user_company
        FOREIGN KEY (user_id) REFERENCES user_company (user_id)
) DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

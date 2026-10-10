-- Feature flags let later phases switch between implementations at runtime
-- (e.g. naive vs. locked transfer) without redeploying, which keeps benchmarks honest.
CREATE TABLE feature_flag
(
    name        VARCHAR(100) NOT NULL,
    enabled     BOOLEAN      NOT NULL DEFAULT FALSE,
    description VARCHAR(255) NULL,
    updated_at  TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_feature_flag PRIMARY KEY (name)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

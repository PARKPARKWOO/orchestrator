CREATE TABLE outbox
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,           -- 고유 ID
    aggregate_id   VARCHAR(255) NOT NULL,                       -- 연관된 Aggregate ID
    aggregate_type VARCHAR(255) NOT NULL,                       -- 연관된 Aggregate 타입 (도메인 구분)
    event_type     VARCHAR(255) NOT NULL,                       -- 이벤트 타입 (e.g., ORDER_CREATED)
    payload        TEXT         NOT NULL,                       -- 이벤트 내용 (JSON 형식)
    status         VARCHAR(50)       DEFAULT 'PENDING',         -- 메시지 상태 (e.g., PENDING, PROCESSED)
    created_at     TIMESTAMP         DEFAULT CURRENT_TIMESTAMP, -- 생성 시간
);

CREATE INDEX idx_status_created_at ON outbox (status, created_at);
CREATE INDEX idx_aggregate_id ON outbox (aggregate_id);

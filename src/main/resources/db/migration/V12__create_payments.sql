CREATE
TYPE payment_status AS ENUM ('PENDING', 'PAID', 'OVERDUE', 'CANCELLED');

CREATE TABLE payments
(
    id BIGSERIAL PRIMARY KEY,
    student_id     BIGINT         NOT NULL REFERENCES students (id) ON DELETE CASCADE,
    study_group_id BIGINT         REFERENCES study_groups (id) ON DELETE SET NULL,
    amount         NUMERIC(10, 2) NOT NULL,
    status payment_status NOT NULL DEFAULT 'PENDING',
    description    VARCHAR(500),
    due_date       DATE,
    paid_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
ALTER TABLE guardian_invitations
    MODIFY COLUMN status ENUM(
        'PENDING',
        'AWAITING_APPROVAL',
        'COMPLETED',
        'EXPIRED',
        'CANCELLED'
    ) NOT NULL;

ALTER TABLE guardian_invitations
    ADD COLUMN guardian_id CHAR(36) NULL AFTER student_id;

ALTER TABLE guardian_invitations
    ADD CONSTRAINT fk_guardian_invitations_guardian
        FOREIGN KEY (guardian_id)
            REFERENCES guardians(id);

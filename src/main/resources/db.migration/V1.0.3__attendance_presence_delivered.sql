ALTER TABLE attendance_students
    MODIFY COLUMN presence ENUM(
        'PRESENT',
        'ABSENT',
        'DELIVERED'
    ) NOT NULL;

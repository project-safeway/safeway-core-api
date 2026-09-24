-- =========================================================
-- USERS
-- =========================================================

CREATE TABLE users (
    id CHAR(36) NOT NULL,
    active BOOLEAN NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    deleted_at DATETIME,

    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash TEXT NOT NULL,

    role ENUM('ADMIN', 'DRIVER', 'GUARDIAN') NOT NULL,

    primary_phone_number CHAR(15) NOT NULL,
    secondary_phone_number CHAR(15),

    PRIMARY KEY (id),
    UNIQUE KEY uk_users_email (email),
);


-- =========================================================
-- ADDRESSES
-- =========================================================

CREATE TABLE addresses (
    id CHAR(36) NOT NULL,
    active BOOLEAN NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    deleted_at DATETIME,

    street VARCHAR(255) NOT NULL,
    number INT NOT NULL,
    additional_details VARCHAR(100),
    neighborhood VARCHAR(100) NOT NULL,
    city VARCHAR(100) NOT NULL,
    federal_unit CHAR(2) NOT NULL,
    zip_code CHAR(9) NOT NULL,

    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),

    type VARCHAR(50),
    principal BOOLEAN,

    PRIMARY KEY (id)
);


-- =========================================================
-- TRANSPORTS
-- =========================================================

CREATE TABLE transports (
    id CHAR(36) NOT NULL,
    active BOOLEAN NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    deleted_at DATETIME,

    license_plate CHAR(7) NOT NULL,
    model VARCHAR(255),
    capacity TINYINT,

    PRIMARY KEY (id),
    UNIQUE KEY uk_transports_license_plate (license_plate)
);


-- =========================================================
-- USER -> TRANSPORT
-- =========================================================
-- Um motorista possui um transporte.
--
-- O relacionamento é mantido aqui porque o transporte
-- pertence ao motorista.
-- =========================================================

ALTER TABLE users
    ADD COLUMN transport_id CHAR(36),
    ADD CONSTRAINT fk_users_transport
        FOREIGN KEY (transport_id)
        REFERENCES transports(id);


-- =========================================================
-- GUARDIANS
-- =========================================================

CREATE TABLE guardians (
    id CHAR(36) NOT NULL,
    active BOOLEAN NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    deleted_at DATETIME,

    name VARCHAR(100) NOT NULL,

    primary_phone_number CHAR(15) NOT NULL,
    secondary_phone_number CHAR(15),

    email VARCHAR(255),

    user_id CHAR(36),
    address_id CHAR(36) NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT fk_guardians_user
       FOREIGN KEY (user_id)
           REFERENCES users(id),

    CONSTRAINT fk_guardians_address
       FOREIGN KEY (address_id)
           REFERENCES addresses(id),

    UNIQUE KEY uk_guardians_user (user_id)
);


-- =========================================================
-- SCHOOLS
-- =========================================================
-- A escola pertence ao transporte.
-- Não pertence diretamente ao usuário.
-- Isso mantém o isolamento dos dados por motorista/transporte.
-- =========================================================

CREATE TABLE schools (
    id CHAR(36) NOT NULL,
    active BOOLEAN NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    deleted_at DATETIME,

    name VARCHAR(100) NOT NULL,

    education_level ENUM('HIGH_SCHOOL', 'MIDDLE_SCHOOL', 'DAYCARE_CENTER', 'KINDERGARTEN'),

    transport_id CHAR(36) NOT NULL,
    address_id CHAR(36) NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT fk_schools_transport
        FOREIGN KEY (transport_id)
            REFERENCES transports(id),

    CONSTRAINT fk_schools_address
        FOREIGN KEY (address_id)
            REFERENCES addresses(id)
);


-- =========================================================
-- STUDENTS
-- =========================================================

CREATE TABLE students (
    id CHAR(36) NOT NULL,
    active BOOLEAN NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    deleted_at DATETIME,

    name VARCHAR(100) NOT NULL,
    professor VARCHAR(150) NOT NULL,
    birthdate DATE,
    grade INT,
    classroom VARCHAR(20),

    monthly_fee DECIMAL(10, 2),
    due_date TINYINT CHECK (due_date BETWEEN 1 AND 31),

    school_id CHAR(36) NOT NULL,
    transport_id CHAR(36) NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT fk_students_school
      FOREIGN KEY (school_id)
          REFERENCES schools(id),

    CONSTRAINT fk_students_transport
      FOREIGN KEY (transport_id)
          REFERENCES transports(id)
);


-- =========================================================
-- GUARDIAN <-> STUDENT
-- =========================================================
-- Um aluno pode possuir vários responsáveis.
-- Um responsável pode possuir vários alunos.
-- =========================================================

CREATE TABLE guardian_students (
    id CHAR(36) NOT NULL,
    active BOOLEAN NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    deleted_at DATETIME,

    guardian_id CHAR(36) NOT NULL,
    student_id CHAR(36) NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT fk_guardian_students_guardian
       FOREIGN KEY (guardian_id)
           REFERENCES guardians(id),

    CONSTRAINT fk_guardian_students_student
       FOREIGN KEY (student_id)
           REFERENCES students(id),

    UNIQUE KEY uk_guardian_students
       (guardian_id, student_id)
);


-- =========================================================
-- ROUTES
-- =========================================================

CREATE TABLE routes (
    id CHAR(36) NOT NULL,
    active BOOLEAN NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    deleted_at DATETIME,

    name VARCHAR(150),
    start_time TIME,
    end_time TIME,

    route_type ENUM('TO_SCHOOL', 'FROM_SCHOOL') NOT NULL,

    transport_id CHAR(36) NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT fk_routes_transport
        FOREIGN KEY (transport_id)
            REFERENCES transports(id)
);


-- =========================================================
-- ROUTE <-> STUDENT
-- =========================================================

CREATE TABLE route_students (
    id CHAR(36) NOT NULL,
    active BOOLEAN NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    deleted_at DATETIME,

    boarding_order INT,
    general_order INT,

    route_id CHAR(36) NOT NULL,
    student_id CHAR(36) NOT NULL,
    address_id CHAR(36) NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT fk_route_students_route
        FOREIGN KEY (route_id)
            REFERENCES routes(id),

    CONSTRAINT fk_route_students_student
        FOREIGN KEY (student_id)
            REFERENCES students(id),

    CONSTRAINT fk_route_students_address
        FOREIGN KEY (address_id)
            REFERENCES addresses(id)
);


-- =========================================================
-- ROUTE <-> SCHOOL
-- =========================================================

CREATE TABLE route_schools (
    id CHAR(36) NOT NULL,
    active BOOLEAN NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    deleted_at DATETIME,

    stop_order INT,
    general_order INT,

    route_id CHAR(36) NOT NULL,
    school_id CHAR(36) NOT NULL,
    address_id CHAR(36) NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT fk_route_schools_route
       FOREIGN KEY (route_id)
           REFERENCES routes(id),

    CONSTRAINT fk_route_schools_school
       FOREIGN KEY (school_id)
           REFERENCES schools(id),

    CONSTRAINT fk_route_schools_address
        FOREIGN KEY (address_id)
           REFERENCES addresses(id)
);


-- =========================================================
-- ATTENDANCES
-- =========================================================

CREATE TABLE attendances (
    id CHAR(36) NOT NULL,
    active BOOLEAN NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    deleted_at DATETIME,

    status ENUM('IN_PROGRESS', 'FINISHED', 'CANCELLED') NOT NULL,

    route_id CHAR(36) NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT fk_attendances_route
        FOREIGN KEY (route_id)
            REFERENCES routes(id)
);


-- =========================================================
-- ATTENDANCE <-> STUDENT
-- =========================================================

CREATE TABLE attendance_students (
    id CHAR(36) NOT NULL,
    active BOOLEAN NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    deleted_at DATETIME,

    presence ENUM('PRESENT','ABSENT') NOT NULL,

    date DATETIME,

    attendance_id CHAR(36) NOT NULL,
    student_id CHAR(36) NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT fk_attendance_students_attendance
        FOREIGN KEY (attendance_id)
            REFERENCES attendances(id),

    CONSTRAINT fk_attendance_students_student
        FOREIGN KEY (student_id)
            REFERENCES students(id)
);


-- =========================================================
-- GUARDIAN REGISTRATION / INVITATIONS
-- =========================================================
--
-- Esta tabela representa o processo de entrada do responsável
-- no sistema, e não o responsável em si.
--
-- Fluxos suportados:
--
-- 1. GENERIC
--    Link genérico do motorista.
--    Não conhece previamente o aluno.
--    Requer aprovação do motorista.
--
-- 2. STUDENT_SPECIFIC
--    Link específico de um aluno.
--    O sistema já conhece o aluno e o transporte.
--    Não requer aprovação adicional.
--
-- 3. GENERIC + PRE-REGISTRATION
--    O motorista já cadastrou dados básicos do responsável.
--    O responsável entra pelo link genérico.
--    O sistema tenta localizar o pré-cadastro por
--    telefone + e-mail.
--    Mesmo havendo correspondência, requer aprovação.
-- =========================================================

CREATE TABLE guardian_invitations (
    id CHAR(36) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    expires_at DATETIME,

    token VARCHAR(255) NOT NULL,

    type ENUM('GENERIC','STUDENT_SPECIFIC') NOT NULL,

    status ENUM('PENDING', 'COMPLETED', 'EXPIRED', 'CANCELLED') NOT NULL,

    transport_id CHAR(36) NOT NULL,
    student_id CHAR(36),

    -- Dados usados para localizar um possível
    -- pré-cadastro de responsável.
    target_email VARCHAR(255),
    target_phone_number CHAR(15),

    created_by_user_id CHAR(36) NOT NULL,

    PRIMARY KEY (id),

    UNIQUE KEY uk_guardian_invitations_token (token),

    CONSTRAINT fk_guardian_invitations_transport
        FOREIGN KEY (transport_id)
            REFERENCES transports(id),

    CONSTRAINT fk_guardian_invitations_student
        FOREIGN KEY (student_id)
            REFERENCES students(id),

    CONSTRAINT fk_guardian_invitations_created_by
        FOREIGN KEY (created_by_user_id)
            REFERENCES users(id)
);
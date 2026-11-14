/*
    Metro Booking - complete SQL Server database setup

    - Creates database [metro] when it does not exist.
    - Creates the schema only when [metro] has no user tables.
    - Never drops tables and never deletes existing data.
    - Matches the current JPA entities used with spring.jpa.hibernate.ddl-auto=validate.
*/

IF DB_ID(N'metro') IS NULL
    EXEC(N'CREATE DATABASE [metro]');
GO

USE [metro];
GO

SET NOCOUNT ON;
SET XACT_ABORT ON;
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
SET ANSI_PADDING ON;
SET ANSI_WARNINGS ON;
SET CONCAT_NULL_YIELDS_NULL ON;
SET ARITHABORT ON;
SET NUMERIC_ROUNDABORT OFF;

IF DB_NAME() <> N'metro'
    THROW 51010, 'Database setup is restricted to the metro database.', 1;

IF EXISTS (SELECT 1 FROM sys.tables WHERE is_ms_shipped = 0)
    THROW 51011, 'The metro database is not empty. No existing table or data was changed.', 1;

BEGIN TRY
    BEGIN TRANSACTION;

    CREATE TABLE dbo.member (
        member_id BIGINT IDENTITY(1,1) NOT NULL,
        email VARCHAR(255) NULL,
        name VARCHAR(255) NULL,
        password VARCHAR(255) NULL,
        phone VARCHAR(255) NULL,
        role VARCHAR(255) NULL,
        school_name VARCHAR(255) NULL,
        status VARCHAR(255) NULL,
        student_card VARCHAR(255) NULL,
        email_normalized AS LOWER(LTRIM(RTRIM(email))) PERSISTED,
        CONSTRAINT pk_member PRIMARY KEY (member_id)
    );

    CREATE TABLE dbo.train_station (
        arrival_time DATE NULL,
        position INT NOT NULL,
        station_id BIGINT IDENTITY(1,1) NOT NULL,
        name NVARCHAR(100) NULL,
        note NVARCHAR(255) NULL,
        CONSTRAINT pk_train_station PRIMARY KEY (station_id)
    );

    CREATE TABLE dbo.blog (
        posting_date DATE NULL,
        admin_id BIGINT NULL,
        blog_id BIGINT IDENTITY(1,1) NOT NULL,
        image_url VARCHAR(5000) NULL,
        content VARCHAR(255) NULL,
        title VARCHAR(255) NULL,
        CONSTRAINT pk_blog PRIMARY KEY (blog_id)
    );

    CREATE TABLE dbo.bus_route (
        bus_id BIGINT IDENTITY(1,1) NOT NULL,
        station_id BIGINT NULL,
        bus_status NVARCHAR(50) NULL,
        location NVARCHAR(100) NULL,
        name NVARCHAR(100) NULL,
        CONSTRAINT pk_bus_route PRIMARY KEY (bus_id)
    );

    CREATE TABLE dbo.bus_route_destination (
        estimated_minutes INT NULL,
        bus_route_id BIGINT NULL,
        id BIGINT IDENTITY(1,1) NOT NULL,
        destination NVARCHAR(100) NULL,
        CONSTRAINT pk_bus_route_destination PRIMARY KEY (id)
    );

    CREATE TABLE dbo.feedback (
        posting_date DATE NULL,
        rating FLOAT(53) NULL,
        feedback_id BIGINT IDENTITY(1,1) NOT NULL,
        member_id BIGINT NULL,
        content VARCHAR(255) NULL,
        title VARCHAR(255) NULL,
        CONSTRAINT pk_feedback PRIMARY KEY (feedback_id)
    );

    CREATE TABLE dbo.oauth_account (
        created_at DATETIMEOFFSET(6) NOT NULL,
        id BIGINT IDENTITY(1,1) NOT NULL,
        member_id BIGINT NOT NULL,
        provider VARCHAR(20) NOT NULL,
        provider_user_id VARCHAR(255) NOT NULL,
        CONSTRAINT pk_oauth_account PRIMARY KEY (id),
        CONSTRAINT uq_oauth_identity UNIQUE (provider, provider_user_id),
        CONSTRAINT uq_oauth_member_provider UNIQUE (member_id, provider)
    );

    CREATE TABLE dbo.payment (
        payment_date DATE NULL,
        price FLOAT(53) NULL,
        member_id BIGINT NULL,
        payment_id BIGINT IDENTITY(1,1) NOT NULL,
        ticket_id BIGINT NULL,
        payment_method VARCHAR(255) NULL,
        status NVARCHAR(255) NULL,
        CONSTRAINT pk_payment PRIMARY KEY (payment_id)
    );

    CREATE TABLE dbo.scan_logs (
        id BIGINT IDENTITY(1,1) NOT NULL,
        scanned_at DATETIME2(6) NOT NULL,
        result VARCHAR(20) NOT NULL,
        scanned_by VARCHAR(100) NULL,
        ticket_code VARCHAR(100) NULL,
        CONSTRAINT pk_scan_logs PRIMARY KEY (id),
        CONSTRAINT ck_scan_result CHECK (
            result IN ('SUCCESS', 'ALREADY_USED', 'EXPIRED', 'CANCELLED', 'NOT_FOUND', 'INVALID_FORMAT')
        )
    );

    CREATE TABLE dbo.scan_tickets (
        expires_at DATETIME2(6) NULL,
        id BIGINT IDENTITY(1,1) NOT NULL,
        issued_at DATETIME2(6) NOT NULL,
        used_at DATETIME2(6) NULL,
        user_id BIGINT NOT NULL,
        status VARCHAR(16) NOT NULL,
        code VARCHAR(32) NOT NULL,
        order_ref VARCHAR(64) NOT NULL,
        used_by VARCHAR(100) NULL,
        event_name NVARCHAR(255) NOT NULL,
        CONSTRAINT pk_scan_tickets PRIMARY KEY (id),
        CONSTRAINT ck_scan_ticket_status CHECK (status IN ('ACTIVE', 'USED', 'CANCELLED')),
        CONSTRAINT uk_scan_ticket_code UNIQUE (code),
        CONSTRAINT uk_scan_ticket_order_ref UNIQUE (order_ref)
    );

    CREATE TABLE dbo.student_verification (
        date_of_birth DATE NULL,
        expires_at DATE NULL,
        issued_at DATE NULL,
        ocr_date_of_birth DATE NULL,
        ocr_expires_at DATE NULL,
        ocr_issued_at DATE NULL,
        created_at DATETIME2(6) NULL,
        id BIGINT IDENTITY(1,1) NOT NULL,
        member_id BIGINT NOT NULL,
        reviewed_at DATETIME2(6) NULL,
        reviewed_by_id BIGINT NULL,
        submitted_at DATETIME2(6) NULL,
        status VARCHAR(20) NOT NULL,
        image_content_type VARCHAR(100) NOT NULL,
        ocr_student_code VARCHAR(100) NULL,
        student_code VARCHAR(100) NOT NULL,
        full_name VARCHAR(150) NOT NULL,
        ocr_full_name VARCHAR(150) NULL,
        ocr_school_name VARCHAR(200) NULL,
        school_name VARCHAR(200) NOT NULL,
        image_path VARCHAR(500) NOT NULL,
        rejection_reason VARCHAR(1000) NULL,
        ocr_text NVARCHAR(MAX) NULL,
        CONSTRAINT pk_student_verification PRIMARY KEY (id)
    );

    CREATE TABLE dbo.ticket_type (
        duration_days INT NULL,
        is_student_ticket BIT NOT NULL
            CONSTRAINT df_ticket_type_is_student_ticket DEFAULT (0),
        price FLOAT(53) NULL,
        station_end_id BIGINT NULL,
        station_start_id BIGINT NULL,
        ticket_type_id BIGINT IDENTITY(1,1) NOT NULL,
        name NVARCHAR(100) NULL,
        CONSTRAINT pk_ticket_type PRIMARY KEY (ticket_type_id)
    );

    CREATE TABLE dbo.train (
        number_people INT NULL,
        admin_id BIGINT NULL,
        train_id BIGINT IDENTITY(1,1) NOT NULL,
        name NVARCHAR(100) NULL,
        status NVARCHAR(50) NULL,
        CONSTRAINT pk_train PRIMARY KEY (train_id)
    );

    CREATE TABLE dbo.train_schedule (
        arrival_time TIME NULL,
        depart_time TIME NULL,
        end_station_id BIGINT NULL,
        schedule_id BIGINT IDENTITY(1,1) NOT NULL,
        start_station_id BIGINT NULL,
        train_id BIGINT NULL,
        CONSTRAINT pk_train_schedule PRIMARY KEY (schedule_id)
    );

    CREATE TABLE dbo.ticket (
        price FLOAT(53) NULL,
        date_end DATETIME2(6) NULL,
        date_start DATETIME2(6) NULL,
        member_id BIGINT NULL,
        schedule_id BIGINT NULL,
        ticket_id BIGINT IDENTITY(1,1) NOT NULL,
        ticket_type_id BIGINT NULL,
        status NVARCHAR(255) NULL,
        CONSTRAINT pk_ticket PRIMARY KEY (ticket_id)
    );

    CREATE UNIQUE INDEX uq_member_email
        ON dbo.member(email) WHERE email IS NOT NULL;
    CREATE UNIQUE INDEX uq_member_email_normalized
        ON dbo.member(email_normalized) WHERE email IS NOT NULL;
    CREATE UNIQUE INDEX uq_payment_ticket
        ON dbo.payment(ticket_id) WHERE ticket_id IS NOT NULL;
    CREATE INDEX idx_scanlog_code ON dbo.scan_logs(ticket_code);
    CREATE INDEX idx_scan_ticket_user ON dbo.scan_tickets(user_id);
    CREATE UNIQUE INDEX ux_student_verification_pending_member
        ON dbo.student_verification(member_id) WHERE status = 'PENDING';
    CREATE UNIQUE INDEX ux_student_verification_draft_member
        ON dbo.student_verification(member_id) WHERE status = 'DRAFT';
    CREATE INDEX ix_student_verification_status_submitted
        ON dbo.student_verification(status, submitted_at DESC);
    CREATE UNIQUE INDEX ux_student_verification_pending_code
        ON dbo.student_verification(student_code) WHERE status = 'PENDING';
    CREATE UNIQUE INDEX ux_student_verification_approved_code
        ON dbo.student_verification(student_code) WHERE status = 'APPROVED';
    CREATE INDEX ix_student_verification_member_created
        ON dbo.student_verification(member_id, created_at DESC);

    ALTER TABLE dbo.blog ADD CONSTRAINT fk_blog_admin
        FOREIGN KEY (admin_id) REFERENCES dbo.member(member_id);
    ALTER TABLE dbo.bus_route ADD CONSTRAINT fk_bus_route_station
        FOREIGN KEY (station_id) REFERENCES dbo.train_station(station_id);
    ALTER TABLE dbo.bus_route_destination ADD CONSTRAINT fk_bus_destination_route
        FOREIGN KEY (bus_route_id) REFERENCES dbo.bus_route(bus_id);
    ALTER TABLE dbo.feedback ADD CONSTRAINT fk_feedback_member
        FOREIGN KEY (member_id) REFERENCES dbo.member(member_id);
    ALTER TABLE dbo.oauth_account ADD CONSTRAINT fk_oauth_member
        FOREIGN KEY (member_id) REFERENCES dbo.member(member_id);
    ALTER TABLE dbo.payment ADD CONSTRAINT fk_payment_member
        FOREIGN KEY (member_id) REFERENCES dbo.member(member_id);
    ALTER TABLE dbo.payment ADD CONSTRAINT fk_payment_ticket
        FOREIGN KEY (ticket_id) REFERENCES dbo.ticket(ticket_id);
    ALTER TABLE dbo.student_verification ADD CONSTRAINT fk_student_verification_member
        FOREIGN KEY (member_id) REFERENCES dbo.member(member_id);
    ALTER TABLE dbo.student_verification ADD CONSTRAINT fk_student_verification_reviewer
        FOREIGN KEY (reviewed_by_id) REFERENCES dbo.member(member_id);
    ALTER TABLE dbo.ticket ADD CONSTRAINT fk_ticket_member
        FOREIGN KEY (member_id) REFERENCES dbo.member(member_id);
    ALTER TABLE dbo.ticket ADD CONSTRAINT fk_ticket_schedule
        FOREIGN KEY (schedule_id) REFERENCES dbo.train_schedule(schedule_id);
    ALTER TABLE dbo.ticket ADD CONSTRAINT fk_ticket_type
        FOREIGN KEY (ticket_type_id) REFERENCES dbo.ticket_type(ticket_type_id);
    ALTER TABLE dbo.ticket_type ADD CONSTRAINT fk_ticket_type_end_station
        FOREIGN KEY (station_end_id) REFERENCES dbo.train_station(station_id);
    ALTER TABLE dbo.ticket_type ADD CONSTRAINT fk_ticket_type_start_station
        FOREIGN KEY (station_start_id) REFERENCES dbo.train_station(station_id);
    ALTER TABLE dbo.train ADD CONSTRAINT fk_train_admin
        FOREIGN KEY (admin_id) REFERENCES dbo.member(member_id);
    ALTER TABLE dbo.train_schedule ADD CONSTRAINT fk_schedule_end_station
        FOREIGN KEY (end_station_id) REFERENCES dbo.train_station(station_id);
    ALTER TABLE dbo.train_schedule ADD CONSTRAINT fk_schedule_start_station
        FOREIGN KEY (start_station_id) REFERENCES dbo.train_station(station_id);
    ALTER TABLE dbo.train_schedule ADD CONSTRAINT fk_schedule_train
        FOREIGN KEY (train_id) REFERENCES dbo.train(train_id);

    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;
    THROW;
END CATCH;
GO

SELECT
    DB_NAME() AS database_name,
    COUNT_BIG(*) AS application_table_count
FROM sys.tables
WHERE is_ms_shipped = 0;
GO

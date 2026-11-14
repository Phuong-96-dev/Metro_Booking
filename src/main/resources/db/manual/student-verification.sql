SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
SET ANSI_PADDING ON;
SET ANSI_WARNINGS ON;
SET CONCAT_NULL_YIELDS_NULL ON;
SET ARITHABORT ON;
SET NUMERIC_ROUNDABORT OFF;
GO

IF COL_LENGTH('dbo.ticket_type', 'is_student_ticket') IS NULL
BEGIN
    ALTER TABLE dbo.ticket_type
        ADD is_student_ticket BIT NOT NULL
            CONSTRAINT DF_ticket_type_is_student_ticket DEFAULT (0);
END;
GO

UPDATE dbo.ticket_type
SET is_student_ticket = 1
WHERE name = N'Ve sinh viên';
GO

IF OBJECT_ID('dbo.student_verification', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.student_verification (
        id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        member_id BIGINT NOT NULL,
        reviewed_by_id BIGINT NULL,
        status NVARCHAR(20) NOT NULL,
        image_path NVARCHAR(500) NOT NULL,
        image_content_type NVARCHAR(100) NOT NULL,
        ocr_text NVARCHAR(MAX) NULL,
        ocr_full_name NVARCHAR(150) NULL,
        ocr_student_code NVARCHAR(100) NULL,
        ocr_school_name NVARCHAR(200) NULL,
        ocr_date_of_birth DATE NULL,
        ocr_issued_at DATE NULL,
        ocr_expires_at DATE NULL,
        full_name NVARCHAR(150) NOT NULL,
        student_code NVARCHAR(100) NOT NULL,
        school_name NVARCHAR(200) NOT NULL,
        date_of_birth DATE NULL,
        issued_at DATE NULL,
        expires_at DATE NULL,
        rejection_reason NVARCHAR(1000) NULL,
        created_at DATETIME2 NOT NULL,
        submitted_at DATETIME2 NULL,
        reviewed_at DATETIME2 NULL,
        CONSTRAINT FK_student_verification_member
            FOREIGN KEY (member_id) REFERENCES dbo.member(member_id),
        CONSTRAINT FK_student_verification_reviewer
            FOREIGN KEY (reviewed_by_id) REFERENCES dbo.member(member_id)
    );
END;
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'UX_student_verification_pending_member'
      AND object_id = OBJECT_ID('dbo.student_verification')
)
BEGIN
    CREATE UNIQUE INDEX UX_student_verification_pending_member
        ON dbo.student_verification(member_id)
        WHERE status = N'PENDING';
END;
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'UX_student_verification_draft_member'
      AND object_id = OBJECT_ID('dbo.student_verification')
)
BEGIN
    CREATE UNIQUE INDEX UX_student_verification_draft_member
        ON dbo.student_verification(member_id)
        WHERE status = N'DRAFT';
END;
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'IX_student_verification_status_submitted'
      AND object_id = OBJECT_ID('dbo.student_verification')
)
BEGIN
    CREATE INDEX IX_student_verification_status_submitted
        ON dbo.student_verification(status, submitted_at DESC);
END;
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'UX_student_verification_pending_code'
      AND object_id = OBJECT_ID('dbo.student_verification')
)
BEGIN
    CREATE UNIQUE INDEX UX_student_verification_pending_code
        ON dbo.student_verification(student_code)
        WHERE status = N'PENDING';
END;
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'UX_student_verification_approved_code'
      AND object_id = OBJECT_ID('dbo.student_verification')
)
BEGIN
    CREATE UNIQUE INDEX UX_student_verification_approved_code
        ON dbo.student_verification(student_code)
        WHERE status = N'APPROVED';
END;
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'IX_student_verification_member_created'
      AND object_id = OBJECT_ID('dbo.student_verification')
)
BEGIN
    CREATE INDEX IX_student_verification_member_created
        ON dbo.student_verification(member_id, created_at DESC);
END;
GO

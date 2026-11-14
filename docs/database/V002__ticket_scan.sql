-- Add QR/barcode ticket scanning to an existing SQL Server Metro database.
-- The migration is additive and idempotent: it does not alter or delete existing business data.
SET XACT_ABORT ON;
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;

IF DB_NAME() <> N'metro'
    THROW 51020, 'Ticket scan migration is restricted to the metro database.', 1;

BEGIN TRANSACTION;

IF OBJECT_ID(N'dbo.scan_tickets', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.scan_tickets (
        id BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT pk_scan_tickets PRIMARY KEY,
        code VARCHAR(32) NOT NULL,
        order_ref VARCHAR(64) NOT NULL,
        user_id BIGINT NOT NULL,
        event_name NVARCHAR(255) NOT NULL,
        status VARCHAR(16) NOT NULL CONSTRAINT ck_scan_ticket_status
            CHECK (status IN ('ACTIVE', 'USED', 'CANCELLED')),
        issued_at DATETIME2(6) NOT NULL,
        expires_at DATETIME2(6) NULL,
        used_at DATETIME2(6) NULL,
        used_by VARCHAR(100) NULL,
        CONSTRAINT uk_scan_ticket_code UNIQUE (code),
        CONSTRAINT uk_scan_ticket_order_ref UNIQUE (order_ref)
    );
END;

IF EXISTS (
    SELECT 1
    FROM sys.columns AS column_info
    JOIN sys.types AS type_info ON type_info.user_type_id = column_info.user_type_id
    WHERE column_info.object_id = OBJECT_ID(N'dbo.scan_tickets')
      AND column_info.name = N'event_name'
      AND type_info.name = N'varchar'
)
    ALTER TABLE dbo.scan_tickets ALTER COLUMN event_name NVARCHAR(255) NOT NULL;

IF NOT EXISTS (SELECT 1 FROM sys.indexes
               WHERE object_id = OBJECT_ID(N'dbo.scan_tickets') AND name = N'idx_scan_ticket_user')
    CREATE INDEX idx_scan_ticket_user ON dbo.scan_tickets(user_id);

IF OBJECT_ID(N'dbo.scan_logs', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.scan_logs (
        id BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT pk_scan_logs PRIMARY KEY,
        ticket_code VARCHAR(100) NULL,
        result VARCHAR(20) NOT NULL CONSTRAINT ck_scan_result
            CHECK (result IN ('SUCCESS', 'ALREADY_USED', 'EXPIRED', 'CANCELLED', 'NOT_FOUND', 'INVALID_FORMAT')),
        scanned_by VARCHAR(100) NULL,
        scanned_at DATETIME2(6) NOT NULL
    );
END;

IF NOT EXISTS (SELECT 1 FROM sys.indexes
               WHERE object_id = OBJECT_ID(N'dbo.scan_logs') AND name = N'idx_scanlog_code')
    CREATE INDEX idx_scanlog_code ON dbo.scan_logs(ticket_code);

-- Backfill codes for Metro tickets that were paid before this feature was installed.
-- 0 and 1 are translated so generated values still match TK[A-Z2-9]{16}.
IF OBJECT_ID(N'dbo.ticket', N'U') IS NOT NULL
   AND OBJECT_ID(N'dbo.ticket_type', N'U') IS NOT NULL
BEGIN
    INSERT INTO dbo.scan_tickets
        (code, order_ref, user_id, event_name, status, issued_at, expires_at, used_at, used_by)
    SELECT
        'TK' + TRANSLATE(UPPER(SUBSTRING(REPLACE(CONVERT(VARCHAR(36), NEWID()), '-', ''), 1, 16)), '01', '23'),
        'METRO-TICKET-' + CONVERT(VARCHAR(30), ticket.ticket_id),
        ticket.member_id,
        COALESCE(ticket_type.name, 'Metro ticket #' + CONVERT(VARCHAR(30), ticket.ticket_id)),
        'ACTIVE',
        COALESCE(ticket.date_start, SYSDATETIME()),
        ticket.date_end,
        NULL,
        NULL
    FROM dbo.ticket AS ticket
    LEFT JOIN dbo.ticket_type AS ticket_type ON ticket_type.ticket_type_id = ticket.ticket_type_id
    WHERE ticket.member_id IS NOT NULL
      AND ticket.status <> N'Chưa thanh toán'
      AND NOT EXISTS (
          SELECT 1 FROM dbo.scan_tickets AS existing
          WHERE existing.order_ref = 'METRO-TICKET-' + CONVERT(VARCHAR(30), ticket.ticket_id)
      );
END;

COMMIT TRANSACTION;

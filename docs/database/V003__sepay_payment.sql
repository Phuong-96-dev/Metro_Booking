/* Add SePay payment metadata to an existing Metro SQL Server database. */
USE [metro];
GO

SET XACT_ABORT ON;
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
GO

IF OBJECT_ID(N'dbo.payment', N'U') IS NULL
    THROW 51030, 'Expected dbo.payment is missing.', 1;
GO

BEGIN TRANSACTION;

IF COL_LENGTH(N'dbo.payment', N'transaction_code') IS NULL
    ALTER TABLE dbo.payment ADD transaction_code VARCHAR(64) NULL;

IF COL_LENGTH(N'dbo.payment', N'sepay_transaction_id') IS NULL
    ALTER TABLE dbo.payment ADD sepay_transaction_id BIGINT NULL;

IF COL_LENGTH(N'dbo.payment', N'payment_content') IS NULL
    ALTER TABLE dbo.payment ADD payment_content NVARCHAR(1000) NULL;

IF COL_LENGTH(N'dbo.payment', N'bank_reference') IS NULL
    ALTER TABLE dbo.payment ADD bank_reference VARCHAR(100) NULL;

IF COL_LENGTH(N'dbo.payment', N'sepay_gateway') IS NULL
    ALTER TABLE dbo.payment ADD sepay_gateway VARCHAR(50) NULL;

IF COL_LENGTH(N'dbo.payment', N'transfer_amount') IS NULL
    ALTER TABLE dbo.payment ADD transfer_amount BIGINT NULL;

IF COL_LENGTH(N'dbo.payment', N'paid_at') IS NULL
    ALTER TABLE dbo.payment ADD paid_at DATETIME2(6) NULL;

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE object_id = OBJECT_ID(N'dbo.payment') AND name = N'uq_payment_transaction_code'
)
    EXEC(N'CREATE UNIQUE INDEX uq_payment_transaction_code
        ON dbo.payment(transaction_code) WHERE transaction_code IS NOT NULL');

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE object_id = OBJECT_ID(N'dbo.payment') AND name = N'uq_payment_sepay_transaction'
)
    EXEC(N'CREATE UNIQUE INDEX uq_payment_sepay_transaction
        ON dbo.payment(sepay_transaction_id) WHERE sepay_transaction_id IS NOT NULL');

COMMIT TRANSACTION;
GO

SELECT name
FROM sys.columns
WHERE object_id = OBJECT_ID(N'dbo.payment')
  AND name IN (N'transaction_code', N'sepay_transaction_id', N'payment_content',
               N'bank_reference', N'sepay_gateway', N'transfer_amount', N'paid_at')
ORDER BY name;
GO

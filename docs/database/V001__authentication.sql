-- SQL Server, manual migration. Review against your actual schema and take a backup first.
-- No application startup hook executes this file. Existing PKs/FKs/data are preserved.
SET XACT_ABORT ON;
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
SET ANSI_PADDING ON;
SET ANSI_WARNINGS ON;
SET CONCAT_NULL_YIELDS_NULL ON;
SET ARITHABORT ON;
SET NUMERIC_ROUNDABORT OFF;

BEGIN TRANSACTION;
IF OBJECT_ID(N'dbo.member', N'U') IS NULL
    THROW 51000, 'Expected dbo.member is missing. Check the existing schema before migrating.', 1;

-- Lock registrations for the duration of the duplicate check and index creation.
IF EXISTS (SELECT LOWER(LTRIM(RTRIM(email))) FROM dbo.member WITH (TABLOCKX, HOLDLOCK)
           WHERE email IS NOT NULL GROUP BY LOWER(LTRIM(RTRIM(email))) HAVING COUNT(*) > 1)
    THROW 51001, 'Duplicate normalized emails exist. Resolve them manually without deleting user data.', 1;

IF COL_LENGTH(N'dbo.member', N'email_normalized') IS NULL
    EXEC(N'ALTER TABLE dbo.member ADD email_normalized AS LOWER(LTRIM(RTRIM(email))) PERSISTED');

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID(N'dbo.member') AND name = N'uq_member_email_normalized')
    EXEC(N'CREATE UNIQUE INDEX uq_member_email_normalized ON dbo.member(email_normalized) WHERE email IS NOT NULL');

-- Existing JPA password column defaults to varchar(255), enough for 60-byte BCrypt hashes.
-- Fail rather than silently truncate hashes on a customized schema.
IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID(N'dbo.member') AND name = N'password'
           AND max_length > 0 AND max_length < 60)
    THROW 51002, 'Password column is too short for BCrypt. Review and expand it before deployment.', 1;

IF OBJECT_ID(N'dbo.oauth_account', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.oauth_account (
        id BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT pk_oauth_account PRIMARY KEY,
        member_id BIGINT NOT NULL,
        provider VARCHAR(20) COLLATE Latin1_General_100_BIN2 NOT NULL,
        provider_user_id VARCHAR(255) COLLATE Latin1_General_100_BIN2 NOT NULL,
        created_at DATETIMEOFFSET(6) NOT NULL CONSTRAINT df_oauth_created_at DEFAULT SYSDATETIMEOFFSET(),
        CONSTRAINT fk_oauth_member FOREIGN KEY (member_id) REFERENCES dbo.member(member_id),
        CONSTRAINT uq_oauth_identity UNIQUE (provider, provider_user_id),
        CONSTRAINT uq_oauth_member_provider UNIQUE (member_id, provider)
    );
END;
COMMIT TRANSACTION;

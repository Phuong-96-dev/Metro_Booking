-- Compatibility rollback: deploy the previous app, retaining OAuth links and all members.
-- The old app ignores oauth_account and email_normalized. No table or row is removed.
-- BCrypt is one-way: the previous plaintext-login implementation cannot authenticate
-- migrated passwords. Prefer rolling forward; restore a reviewed app version supporting BCrypt.
-- Only remove this migration's email uniqueness index if that is explicitly needed:
SET XACT_ABORT ON;
BEGIN TRANSACTION;
IF EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID(N'dbo.member') AND name = N'uq_member_email_normalized')
    DROP INDEX uq_member_email_normalized ON dbo.member;
COMMIT TRANSACTION;
-- Keep email_normalized and oauth_account so rollback never loses identity/link data.

-- Metro SQL Server setup for SQL Server Management Studio (SSMS).
-- Creates metro only if absent. Existing tables are never dropped.
-- Initial schema requires a completely empty metro database.
-- No credentials, accounts, or sample business data are included.
IF DB_ID(N'metro') IS NULL
    EXEC(N'CREATE DATABASE [metro]');
GO
USE [metro];
GO
-- Initial SQL Server schema exported from the current JPA entities.
-- Run only against a new, empty local database named metro.
-- Then run V001__authentication.sql to add OAuth storage and normalized email uniqueness.
-- No tables are dropped and no seed users or business data are inserted.
SET XACT_ABORT ON;
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
SET ANSI_PADDING ON;
SET ANSI_WARNINGS ON;
SET CONCAT_NULL_YIELDS_NULL ON;
SET ARITHABORT ON;
SET NUMERIC_ROUNDABORT OFF;
IF DB_NAME() <> N'metro'
    THROW 51010, 'Initial schema is restricted to the metro database.', 1;
IF EXISTS (SELECT 1 FROM sys.tables WHERE is_ms_shipped = 0)
    THROW 51011, 'Initial schema requires an empty database; existing tables are preserved.', 1;
BEGIN TRANSACTION;
create table blog (posting_date date, admin_id bigint, blog_id bigint identity not null, image_url varchar(5000), content varchar(255), title varchar(255), primary key (blog_id));
create table bus_route (bus_id bigint identity not null, station_id bigint, bus_status NVARCHAR(50), location NVARCHAR(100), name NVARCHAR(100), primary key (bus_id));
create table bus_route_destination (estimated_minutes int, bus_route_id bigint, id bigint identity not null, destination NVARCHAR(100), primary key (id));
create table feedback (posting_date date, rating float(53), feedback_id bigint identity not null, member_id bigint, content varchar(255), title varchar(255), primary key (feedback_id));
create table member (member_id bigint identity not null, email varchar(255), name varchar(255), password varchar(255), phone varchar(255), role varchar(255), school_name varchar(255), status varchar(255), student_card varchar(255), primary key (member_id));
create table payment (payment_date date, price float(53), member_id bigint, payment_id bigint identity not null, ticket_id bigint, payment_method varchar(255), status NVARCHAR(255), primary key (payment_id));
create table scan_logs (id bigint identity not null, scanned_at datetime2(6) not null, scanned_by varchar(100), ticket_code varchar(100), result varchar(20) not null check (result in ('SUCCESS','ALREADY_USED','EXPIRED','CANCELLED','NOT_FOUND','INVALID_FORMAT')), primary key (id));
create table scan_tickets (id bigint identity not null, code varchar(32) not null, event_name NVARCHAR(255) not null, expires_at datetime2(6), issued_at datetime2(6) not null, order_ref varchar(64) not null, status varchar(16) not null check (status in ('ACTIVE','USED','CANCELLED')), used_at datetime2(6), user_id bigint not null, used_by varchar(100), primary key (id));
create table ticket (price float(53), date_end datetime2(6), date_start datetime2(6), member_id bigint, schedule_id bigint, ticket_id bigint identity not null, ticket_type_id bigint, status NVARCHAR(255), primary key (ticket_id));
create table ticket_type (duration_days int, price float(53), station_end_id bigint, station_start_id bigint, ticket_type_id bigint identity not null, name NVARCHAR(100), primary key (ticket_type_id));
create table train (number_people int, admin_id bigint, train_id bigint identity not null, name NVARCHAR(100), status NVARCHAR(50), primary key (train_id));
create table train_schedule (arrival_time time, depart_time time, end_station_id bigint, schedule_id bigint identity not null, start_station_id bigint, train_id bigint, primary key (schedule_id));
create table train_station (arrival_time date, position int not null, station_id bigint identity not null, name NVARCHAR(100), note NVARCHAR(255), primary key (station_id));
create unique nonclustered index UKmbmcqelty0fbrvxp1q58dn57t on member (email) where email is not null;
create unique nonclustered index UK7nggx4t0kjjwi9xg06jm2pwft on payment (ticket_id) where ticket_id is not null;
create index idx_scanlog_code on scan_logs (ticket_code);
create unique index uk_scan_ticket_code on scan_tickets (code);
create unique index uk_scan_ticket_order_ref on scan_tickets (order_ref);
create index idx_scan_ticket_user on scan_tickets (user_id);
alter table blog add constraint FKdmxhhegjnn6i13skgdmvtuufw foreign key (admin_id) references member;
alter table bus_route add constraint FKb0f8c4iolv777bgmm37rifa1q foreign key (station_id) references train_station;
alter table bus_route_destination add constraint FK9frvo0h70fs9waexo4pi46kum foreign key (bus_route_id) references bus_route;
alter table feedback add constraint FKmonjtjt92g6gruqyfumtmg8m8 foreign key (member_id) references member;
alter table payment add constraint FK4pswry4r5sx6j57cdeulh1hx8 foreign key (member_id) references member;
alter table payment add constraint FKknnc0dgh1i8fkalxi2qcj2bwl foreign key (ticket_id) references ticket;
alter table ticket add constraint FKd5t3axsm3pj3mt41qhpkmw9yj foreign key (member_id) references member;
alter table ticket add constraint FK1xpu43qjvqe1b278l512ywttq foreign key (schedule_id) references train_schedule;
alter table ticket add constraint FKicdt7jp8dniw1fw6y8y0u9gcr foreign key (ticket_type_id) references ticket_type;
alter table ticket_type add constraint FK87bftkroh8ytn0rsk6bcwo78g foreign key (station_end_id) references train_station;
alter table ticket_type add constraint FKn9063gxjh8boc9mirc8gfmedb foreign key (station_start_id) references train_station;
alter table train add constraint FK1b0xyfisa9tfgfmstdldyf9do foreign key (admin_id) references member;
alter table train_schedule add constraint FKh37tcn2r3ius0rfrnlbscsqnd foreign key (end_station_id) references train_station;
alter table train_schedule add constraint FKl5h1vuk3afh07vmfmtbw0qsgy foreign key (start_station_id) references train_station;
alter table train_schedule add constraint FKievh47dn31fxyth9oc0sactio foreign key (train_id) references train;
COMMIT TRANSACTION;

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

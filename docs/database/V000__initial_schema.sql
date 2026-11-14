-- Initial SQL Server schema exported from the current JPA entities.
-- Run only against a new, empty local database named metro.
-- Then run V001__authentication.sql to add normalized email uniqueness.
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
create table oauth_account (created_at datetimeoffset(6) not null, id bigint identity not null, member_id bigint not null, provider varchar(20) not null, provider_user_id varchar(255) not null, primary key (id));
create table payment (payment_date date, price float(53), member_id bigint, payment_id bigint identity not null, ticket_id bigint, payment_method varchar(255), status NVARCHAR(255), primary key (payment_id));
create table scan_logs (id bigint identity not null, scanned_at datetime2(6) not null, scanned_by varchar(100), ticket_code varchar(100), result varchar(20) not null check (result in ('SUCCESS','ALREADY_USED','EXPIRED','CANCELLED','NOT_FOUND','INVALID_FORMAT')), primary key (id));
create table scan_tickets (id bigint identity not null, code varchar(32) not null, event_name NVARCHAR(255) not null, expires_at datetime2(6), issued_at datetime2(6) not null, order_ref varchar(64) not null, status varchar(16) not null check (status in ('ACTIVE','USED','CANCELLED')), used_at datetime2(6), user_id bigint not null, used_by varchar(100), primary key (id));
create table student_verification (date_of_birth date, expires_at date, issued_at date, ocr_date_of_birth date, ocr_expires_at date, ocr_issued_at date, created_at datetime2(6), id bigint identity not null, member_id bigint not null, reviewed_at datetime2(6), reviewed_by_id bigint, submitted_at datetime2(6), status varchar(20) not null, image_content_type varchar(100) not null, ocr_student_code varchar(100), student_code varchar(100) not null, full_name varchar(150) not null, ocr_full_name varchar(150), ocr_school_name varchar(200), school_name varchar(200) not null, image_path varchar(500) not null, rejection_reason varchar(1000), ocr_text NVARCHAR(MAX), primary key (id));
create table ticket (price float(53), date_end datetime2(6), date_start datetime2(6), member_id bigint, schedule_id bigint, ticket_id bigint identity not null, ticket_type_id bigint, status NVARCHAR(255), primary key (ticket_id));
create table ticket_type (duration_days int, is_student_ticket bit not null constraint DF_ticket_type_is_student_ticket default (0), price float(53), station_end_id bigint, station_start_id bigint, ticket_type_id bigint identity not null, name NVARCHAR(100), primary key (ticket_type_id));
create table train (number_people int, admin_id bigint, train_id bigint identity not null, name NVARCHAR(100), status NVARCHAR(50), primary key (train_id));
create table train_schedule (arrival_time time, depart_time time, end_station_id bigint, schedule_id bigint identity not null, start_station_id bigint, train_id bigint, primary key (schedule_id));
create table train_station (arrival_time date, position int not null, station_id bigint identity not null, name NVARCHAR(100), note NVARCHAR(255), primary key (station_id));
create unique nonclustered index UKmbmcqelty0fbrvxp1q58dn57t on member (email) where email is not null;
alter table oauth_account add constraint uq_oauth_identity unique (provider, provider_user_id);
alter table oauth_account add constraint uq_oauth_member_provider unique (member_id, provider);
create unique nonclustered index UK7nggx4t0kjjwi9xg06jm2pwft on payment (ticket_id) where ticket_id is not null;
create index idx_scanlog_code on scan_logs (ticket_code);
create unique index uk_scan_ticket_code on scan_tickets (code);
create unique index uk_scan_ticket_order_ref on scan_tickets (order_ref);
create index idx_scan_ticket_user on scan_tickets (user_id);
create unique index UX_student_verification_pending_member on student_verification(member_id) where status = 'PENDING';
create unique index UX_student_verification_draft_member on student_verification(member_id) where status = 'DRAFT';
create index IX_student_verification_status_submitted on student_verification(status, submitted_at desc);
create unique index UX_student_verification_pending_code on student_verification(student_code) where status = 'PENDING';
create unique index UX_student_verification_approved_code on student_verification(student_code) where status = 'APPROVED';
create index IX_student_verification_member_created on student_verification(member_id, created_at desc);
alter table blog add constraint FKdmxhhegjnn6i13skgdmvtuufw foreign key (admin_id) references member;
alter table bus_route add constraint FKb0f8c4iolv777bgmm37rifa1q foreign key (station_id) references train_station;
alter table bus_route_destination add constraint FK9frvo0h70fs9waexo4pi46kum foreign key (bus_route_id) references bus_route;
alter table feedback add constraint FKmonjtjt92g6gruqyfumtmg8m8 foreign key (member_id) references member;
alter table oauth_account add constraint FK75dxauq72cuflu7n4seacd4rf foreign key (member_id) references member;
alter table payment add constraint FK4pswry4r5sx6j57cdeulh1hx8 foreign key (member_id) references member;
alter table payment add constraint FKknnc0dgh1i8fkalxi2qcj2bwl foreign key (ticket_id) references ticket;
alter table student_verification add constraint FKiiorcranqv4mgwtamiroira66 foreign key (member_id) references member;
alter table student_verification add constraint FKaip7xh4blfa0d2q04m6dsgvq6 foreign key (reviewed_by_id) references member;
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

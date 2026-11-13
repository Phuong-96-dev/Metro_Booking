/*
    METRO BOOKING - DEMO SEED DATA
    Database: metro (SQL Server)
    Purpose:
      - Seed demo admin/member
      - Seed 14 Metro Line 1 stations
      - Seed trains and schedules in both directions
      - Seed route-based ticket types + time-based ticket types
      - Seed one sample PENDING ticket/payment for SePay testing

    IMPORTANT:
      1. Run this script against database [metro].
      2. This script does NOT delete existing records.
      3. Demo passwords are plain text because the current MemberServiceImpl
         compares email/password directly. Do not use this approach in production.
      4. The source ZIP provided does not contain the SePay additions in Payment.java
         or the SePay webhook classes. The final PENDING Payment insert below uses
         the columns present in your live SQL schema. Verify the running source
         maps transactionCode/paymentContent/sepayTransactionId before testing webhook.
*/

USE [metro];
GO

SET NOCOUNT ON;
SET XACT_ABORT ON;

BEGIN TRY
    BEGIN TRANSACTION;

    /* =========================================================
       1. MEMBERS
       ========================================================= */
    IF NOT EXISTS (SELECT 1 FROM dbo.member WHERE email = 'admin@metro.demo')
    BEGIN
        INSERT INTO dbo.member
            (name, student_card, email, phone, school_name, password, role, status)
        VALUES
            (N'Metro Administrator', 'ADMIN-DEMO-001', 'admin@metro.demo',
             '0900000001', N'Metro Booking Demo', 'Admin@123', 'ADMIN', 'ACTIVE');
    END;

    IF NOT EXISTS (SELECT 1 FROM dbo.member WHERE email = 'member@metro.demo')
    BEGIN
        INSERT INTO dbo.member
            (name, student_card, email, phone, school_name, password, role, status)
        VALUES
            (N'Demo Customer', 'MEMBER-DEMO-001', 'member@metro.demo',
             '0900000002', N'Demo University', 'Member@123', 'MEMBER', 'ACTIVE');
    END;

    DECLARE @AdminId BIGINT =
        (SELECT TOP 1 member_id FROM dbo.member WHERE email = 'admin@metro.demo');

    DECLARE @MemberId BIGINT =
        (SELECT TOP 1 member_id FROM dbo.member WHERE email = 'member@metro.demo');

    /* =========================================================
       2. TRAIN STATIONS - METRO LINE 1
       Includes Van Thanh (the 14-station list).
       ========================================================= */
    DECLARE @Stations TABLE
    (
        position INT NOT NULL,
        station_name NVARCHAR(100) NOT NULL,
        station_note NVARCHAR(255) NULL
    );

    INSERT INTO @Stations (position, station_name, station_note) VALUES (1, N'Bến Thành', N'Ga đầu tuyến');
    INSERT INTO @Stations (position, station_name, station_note) VALUES (2, N'Nhà hát Thành phố', N'Ga trung tâm');
    INSERT INTO @Stations (position, station_name, station_note) VALUES (3, N'Ba Son', N'Ga trung tâm');
    INSERT INTO @Stations (position, station_name, station_note) VALUES (4, N'Văn Thánh', N'Ga trên cao');
    INSERT INTO @Stations (position, station_name, station_note) VALUES (5, N'Tân Cảng', N'Ga trên cao');
    INSERT INTO @Stations (position, station_name, station_note) VALUES (6, N'Thảo Điền', N'Ga trên cao');
    INSERT INTO @Stations (position, station_name, station_note) VALUES (7, N'An Phú', N'Ga trên cao');
    INSERT INTO @Stations (position, station_name, station_note) VALUES (8, N'Rạch Chiếc', N'Ga trên cao');
    INSERT INTO @Stations (position, station_name, station_note) VALUES (9, N'Phước Long', N'Ga trên cao');
    INSERT INTO @Stations (position, station_name, station_note) VALUES (10, N'Bình Thái', N'Ga trên cao');
    INSERT INTO @Stations (position, station_name, station_note) VALUES (11, N'Thủ Đức', N'Ga trên cao');
    INSERT INTO @Stations (position, station_name, station_note) VALUES (12, N'Khu Công nghệ cao', N'Ga trên cao');
    INSERT INTO @Stations (position, station_name, station_note) VALUES (13, N'Đại học Quốc gia', N'Ga trên cao');
    INSERT INTO @Stations (position, station_name, station_note) VALUES (14, N'Bến xe Suối Tiên', N'Ga cuối tuyến');

    INSERT INTO dbo.train_station (name, arrival_time, note, position)
    SELECT s.station_name, NULL, s.station_note, s.position
    FROM @Stations s
    WHERE NOT EXISTS
    (
        SELECT 1
        FROM dbo.train_station ts
        WHERE ts.position = s.position
           OR ts.name = s.station_name
    );

    /* =========================================================
       3. TRAINS
       ========================================================= */
    IF NOT EXISTS (SELECT 1 FROM dbo.train WHERE name = N'Metro Demo Train 01')
    BEGIN
        INSERT INTO dbo.train (name, number_people, status, admin_id)
        VALUES (N'Metro Demo Train 01', 930, N'ACTIVE', @AdminId);
    END;

    IF NOT EXISTS (SELECT 1 FROM dbo.train WHERE name = N'Metro Demo Train 02')
    BEGIN
        INSERT INTO dbo.train (name, number_people, status, admin_id)
        VALUES (N'Metro Demo Train 02', 930, N'ACTIVE', @AdminId);
    END;

    DECLARE @Train1 BIGINT =
        (SELECT TOP 1 train_id FROM dbo.train WHERE name = N'Metro Demo Train 01');
    DECLARE @Train2 BIGINT =
        (SELECT TOP 1 train_id FROM dbo.train WHERE name = N'Metro Demo Train 02');

    DECLARE @FirstStation BIGINT =
        (SELECT TOP 1 station_id FROM dbo.train_station WHERE name = N'Bến Thành');
    DECLARE @LastStation BIGINT =
        (SELECT TOP 1 station_id FROM dbo.train_station WHERE name = N'Bến xe Suối Tiên');

    /* =========================================================
       4. TRAIN SCHEDULES - FULL ROUTE, BOTH DIRECTIONS
       TrainScheduleServiceImpl expects schedules between terminal stations.
       Each schedule represents a departure time.
       ========================================================= */
    DECLARE @ScheduleSeeds TABLE
    (
        train_id BIGINT,
        start_station_id BIGINT,
        end_station_id BIGINT,
        depart_time TIME(7)
    );

    INSERT INTO @ScheduleSeeds (train_id, start_station_id, end_station_id, depart_time) VALUES (@Train1, @FirstStation, @LastStation, CAST('06:00' AS TIME));
    INSERT INTO @ScheduleSeeds (train_id, start_station_id, end_station_id, depart_time) VALUES (@Train1, @FirstStation, @LastStation, CAST('07:00' AS TIME));
    INSERT INTO @ScheduleSeeds (train_id, start_station_id, end_station_id, depart_time) VALUES (@Train1, @FirstStation, @LastStation, CAST('08:00' AS TIME));
    INSERT INTO @ScheduleSeeds (train_id, start_station_id, end_station_id, depart_time) VALUES (@Train1, @FirstStation, @LastStation, CAST('09:00' AS TIME));
    INSERT INTO @ScheduleSeeds (train_id, start_station_id, end_station_id, depart_time) VALUES (@Train1, @LastStation, @FirstStation, CAST('06:30' AS TIME));
    INSERT INTO @ScheduleSeeds (train_id, start_station_id, end_station_id, depart_time) VALUES (@Train1, @LastStation, @FirstStation, CAST('07:30' AS TIME));
    INSERT INTO @ScheduleSeeds (train_id, start_station_id, end_station_id, depart_time) VALUES (@Train1, @LastStation, @FirstStation, CAST('08:30' AS TIME));
    INSERT INTO @ScheduleSeeds (train_id, start_station_id, end_station_id, depart_time) VALUES (@Train1, @LastStation, @FirstStation, CAST('09:30' AS TIME));
    INSERT INTO @ScheduleSeeds (train_id, start_station_id, end_station_id, depart_time) VALUES (@Train2, @FirstStation, @LastStation, CAST('10:00' AS TIME));
    INSERT INTO @ScheduleSeeds (train_id, start_station_id, end_station_id, depart_time) VALUES (@Train2, @FirstStation, @LastStation, CAST('11:00' AS TIME));
    INSERT INTO @ScheduleSeeds (train_id, start_station_id, end_station_id, depart_time) VALUES (@Train2, @FirstStation, @LastStation, CAST('16:00' AS TIME));
    INSERT INTO @ScheduleSeeds (train_id, start_station_id, end_station_id, depart_time) VALUES (@Train2, @FirstStation, @LastStation, CAST('17:00' AS TIME));
    INSERT INTO @ScheduleSeeds (train_id, start_station_id, end_station_id, depart_time) VALUES (@Train2, @LastStation, @FirstStation, CAST('10:30' AS TIME));
    INSERT INTO @ScheduleSeeds (train_id, start_station_id, end_station_id, depart_time) VALUES (@Train2, @LastStation, @FirstStation, CAST('11:30' AS TIME));
    INSERT INTO @ScheduleSeeds (train_id, start_station_id, end_station_id, depart_time) VALUES (@Train2, @LastStation, @FirstStation, CAST('16:30' AS TIME));
    INSERT INTO @ScheduleSeeds (train_id, start_station_id, end_station_id, depart_time) VALUES (@Train2, @LastStation, @FirstStation, CAST('17:30' AS TIME));

    INSERT INTO dbo.train_schedule
        (depart_time, arrival_time, start_station_id, end_station_id, train_id)
    SELECT
        ss.depart_time,
        DATEADD(MINUTE, 39, CAST(ss.depart_time AS DATETIME)),
        ss.start_station_id,
        ss.end_station_id,
        ss.train_id
    FROM @ScheduleSeeds ss
    WHERE NOT EXISTS
    (
        SELECT 1
        FROM dbo.train_schedule existing
        WHERE existing.train_id = ss.train_id
          AND existing.start_station_id = ss.start_station_id
          AND existing.end_station_id = ss.end_station_id
          AND existing.depart_time = ss.depart_time
    );

    /* =========================================================
       5. ROUTE-BASED TICKET TYPES
       TicketController expects the exact name N'Vé lượt'.
       Add all distinct station pairs, both directions.
       Demo fare: 7,000 + 1,000 per station interval.
       ========================================================= */
    INSERT INTO dbo.ticket_type
        (name, price, duration_days, station_start_id, station_end_id)
    SELECT
        N'Vé lượt',
        CAST(7000 + (ABS(start_station.position - end_station.position) * 1000) AS FLOAT),
        0,
        start_station.station_id,
        end_station.station_id
    FROM dbo.train_station start_station
    CROSS JOIN dbo.train_station end_station
    WHERE start_station.position <> end_station.position
      AND start_station.position BETWEEN 1 AND 14
      AND end_station.position BETWEEN 1 AND 14
      AND NOT EXISTS
      (
          SELECT 1
          FROM dbo.ticket_type tt
          WHERE tt.station_start_id = start_station.station_id
            AND tt.station_end_id = end_station.station_id
            AND tt.name = N'Vé lượt'
      );

    /* =========================================================
       6. TIME-BASED TICKET TYPES
       TicketController shows ticket types with both station IDs NULL
       in search results.
       ========================================================= */
    IF NOT EXISTS
       (SELECT 1 FROM dbo.ticket_type WHERE name = N'Vé ngày' AND station_start_id IS NULL AND station_end_id IS NULL)
    BEGIN
        INSERT INTO dbo.ticket_type
            (name, price, duration_days, station_start_id, station_end_id)
        VALUES (N'Vé ngày', 40000, 1, NULL, NULL);
    END;

    IF NOT EXISTS
       (SELECT 1 FROM dbo.ticket_type WHERE name = N'Vé 3 ngày' AND station_start_id IS NULL AND station_end_id IS NULL)
    BEGIN
        INSERT INTO dbo.ticket_type
            (name, price, duration_days, station_start_id, station_end_id)
        VALUES (N'Vé 3 ngày', 90000, 3, NULL, NULL);
    END;

    IF NOT EXISTS
       (SELECT 1 FROM dbo.ticket_type WHERE name = N'Vé tháng' AND station_start_id IS NULL AND station_end_id IS NULL)
    BEGIN
        INSERT INTO dbo.ticket_type
            (name, price, duration_days, station_start_id, station_end_id)
        VALUES (N'Vé tháng', 300000, 30, NULL, NULL);
    END;

    /* =========================================================
       7. DEMO BUS ROUTES + DESTINATIONS
       Optional supporting data for the bus route screens.
       ========================================================= */
    DECLARE @BusSeeds TABLE
    (
        bus_name NVARCHAR(100),
        location NVARCHAR(100),
        station_name NVARCHAR(100),
        destination NVARCHAR(100),
        estimated_minutes INT
    );

    INSERT INTO @BusSeeds (bus_name, location, station_name, destination, estimated_minutes) VALUES (N'Bus connection - Ben Thanh', N'Quận 1', N'Bến Thành', N'Chợ Bến Thành', 5);
    INSERT INTO @BusSeeds (bus_name, location, station_name, destination, estimated_minutes) VALUES (N'Bus connection - Van Thanh', N'Bình Thạnh', N'Văn Thánh', N'Khu du lịch Văn Thánh', 8);
    INSERT INTO @BusSeeds (bus_name, location, station_name, destination, estimated_minutes) VALUES (N'Bus connection - Thu Duc', N'Thủ Đức', N'Thủ Đức', N'Chợ Thủ Đức', 10);
    INSERT INTO @BusSeeds (bus_name, location, station_name, destination, estimated_minutes) VALUES (N'Bus connection - National University', N'Thủ Đức', N'Đại học Quốc gia', N'Làng Đại học', 7);
    INSERT INTO @BusSeeds (bus_name, location, station_name, destination, estimated_minutes) VALUES (N'Bus connection - Suoi Tien', N'Thủ Đức', N'Bến xe Suối Tiên', N'Khu du lịch Suối Tiên', 5);

    INSERT INTO dbo.bus_route (name, location, bus_status, station_id)
    SELECT b.bus_name, b.location, N'ACTIVE', ts.station_id
    FROM @BusSeeds b
    JOIN dbo.train_station ts ON ts.name = b.station_name
    WHERE NOT EXISTS
    (
        SELECT 1 FROM dbo.bus_route br WHERE br.name = b.bus_name
    );

    INSERT INTO dbo.bus_route_destination (bus_route_id, destination, estimated_minutes)
    SELECT br.bus_id, b.destination, b.estimated_minutes
    FROM @BusSeeds b
    JOIN dbo.bus_route br ON br.name = b.bus_name
    WHERE NOT EXISTS
    (
        SELECT 1
        FROM dbo.bus_route_destination d
        WHERE d.bus_route_id = br.bus_id
          AND d.destination = b.destination
    );

    /* =========================================================
       8. ONE SAMPLE PENDING TICKET + PAYMENT FOR SEPAY
       Insert only if this demo payment does not already exist.
       It uses the first full-route schedule and first route ticket type.
       The live SePay service should match transaction_code exactly.
       ========================================================= */
    DECLARE @DemoTicketId BIGINT;
    DECLARE @DemoScheduleId BIGINT =
    (
        SELECT TOP 1 schedule_id
        FROM dbo.train_schedule
        WHERE start_station_id = @FirstStation
          AND end_station_id = @LastStation
        ORDER BY depart_time
    );

    DECLARE @DemoTicketTypeId BIGINT =
    (
        SELECT TOP 1 ticket_type_id
        FROM dbo.ticket_type
        WHERE name = N'Vé lượt'
          AND station_start_id = @FirstStation
          AND station_end_id = @LastStation
        ORDER BY ticket_type_id
    );

    IF NOT EXISTS
       (SELECT 1 FROM dbo.payment WHERE transaction_code = 'METRODEMO1001')
    BEGIN
        INSERT INTO dbo.ticket
            (status, date_start, date_end, price, member_id, schedule_id, ticket_type_id)
        VALUES
            (N'Chờ thanh toán', SYSDATETIME(), DATEADD(HOUR, 3, SYSDATETIME()),
             (SELECT price FROM dbo.ticket_type WHERE ticket_type_id = @DemoTicketTypeId),
             @MemberId, @DemoScheduleId, @DemoTicketTypeId);

        SET @DemoTicketId = SCOPE_IDENTITY();

        INSERT INTO dbo.payment
            (payment_date, payment_method, price, status, member_id, ticket_id,
             payment_content, sepay_transaction_id, transaction_code)
        SELECT
            NULL,
            'SEPAY',
            t.price,
            N'PENDING',
            @MemberId,
            @DemoTicketId,
            'METRODEMO1001',
            NULL,
            'METRODEMO1001'
        FROM dbo.ticket t
        WHERE t.ticket_id = @DemoTicketId;
    END;

    COMMIT TRANSACTION;

    PRINT 'Metro Booking demo seed completed successfully.';
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;

    THROW;
END CATCH;
GO

/* =============================================================
   9. VERIFY SEEDED DATA
   ============================================================= */
SELECT 'member' AS table_name, COUNT_BIG(*) AS total_rows FROM dbo.member
UNION ALL SELECT 'train_station', COUNT_BIG(*) FROM dbo.train_station
UNION ALL SELECT 'train', COUNT_BIG(*) FROM dbo.train
UNION ALL SELECT 'train_schedule', COUNT_BIG(*) FROM dbo.train_schedule
UNION ALL SELECT 'ticket_type', COUNT_BIG(*) FROM dbo.ticket_type
UNION ALL SELECT 'bus_route', COUNT_BIG(*) FROM dbo.bus_route
UNION ALL SELECT 'bus_route_destination', COUNT_BIG(*) FROM dbo.bus_route_destination
UNION ALL SELECT 'ticket', COUNT_BIG(*) FROM dbo.ticket
UNION ALL SELECT 'payment', COUNT_BIG(*) FROM dbo.payment;

SELECT
    member_id, name, email, role, status
FROM dbo.member
WHERE email IN ('admin@metro.demo', 'member@metro.demo');

SELECT
    station_id, position, name
FROM dbo.train_station
WHERE position BETWEEN 1 AND 14
ORDER BY position;

SELECT
    p.payment_id,
    p.transaction_code,
    p.payment_content,
    p.price,
    p.status,
    p.ticket_id,
    t.status AS ticket_status
FROM dbo.payment p
LEFT JOIN dbo.ticket t ON t.ticket_id = p.ticket_id
WHERE p.transaction_code = 'METRODEMO1001';

SET NAMES utf8mb4;
CREATE DATABASE IF NOT EXISTS sportsphere CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE sportsphere;

CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    first_name VARCHAR(80) NOT NULL,
    last_name VARCHAR(80) NOT NULL,
    phone VARCHAR(30) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    profile_image_path VARCHAR(500),
    role ENUM('ATHLETE', 'EMPLOYEE', 'ADMIN') NOT NULL,
    status ENUM('PENDING', 'ACTIVE', 'REJECTED', 'DISABLED') NOT NULL DEFAULT 'PENDING',
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE sports (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(80) NOT NULL UNIQUE,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE user_favorite_sports (
    user_id BIGINT NOT NULL,
    sport_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, sport_id),
    CONSTRAINT fk_favorite_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_favorite_sport FOREIGN KEY (sport_id) REFERENCES sports(id)
);

CREATE TABLE employee_organizations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(160) NOT NULL,
    registered_address VARCHAR(255) NOT NULL,
    registration_number CHAR(8) NOT NULL UNIQUE,
    tax_id CHAR(9) NOT NULL UNIQUE,
    CONSTRAINT chk_registration_number CHECK (registration_number REGEXP '^[0-9]{8}$'),
    CONSTRAINT chk_tax_id CHECK (tax_id REGEXP '^[1-9][0-9]{8}$')
);

CREATE TABLE organization_employees (
    organization_id BIGINT NOT NULL,
    employee_id BIGINT NOT NULL UNIQUE,
    PRIMARY KEY (organization_id, employee_id),
    CONSTRAINT fk_org_employee_org FOREIGN KEY (organization_id) REFERENCES employee_organizations(id),
    CONSTRAINT fk_org_employee_user FOREIGN KEY (employee_id) REFERENCES users(id)
);

CREATE TABLE facilities (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    organization_id BIGINT NOT NULL,
    name VARCHAR(160) NOT NULL,
    city VARCHAR(100) NOT NULL,
    address VARCHAR(255) NOT NULL,
    description TEXT,
    latitude DECIMAL(10, 7),
    longitude DECIMAL(10, 7),
    opening_time TIME NOT NULL,
    closing_time TIME NOT NULL,
    allowed_no_shows INT NOT NULL DEFAULT 3,
    status ENUM('PENDING', 'ACTIVE', 'REJECTED', 'INACTIVE') NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_facility_org_name (organization_id, name),
    CONSTRAINT fk_facility_org FOREIGN KEY (organization_id) REFERENCES employee_organizations(id),
    CONSTRAINT chk_allowed_no_shows CHECK (allowed_no_shows >= 1)
);

CREATE TABLE facility_images (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    facility_id BIGINT NOT NULL,
    image_path VARCHAR(500) NOT NULL,
    display_order INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_image_facility FOREIGN KEY (facility_id) REFERENCES facilities(id) ON DELETE CASCADE
);

CREATE TABLE resources (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    facility_id BIGINT NOT NULL,
    name VARCHAR(120) NOT NULL,
    type ENUM('OUTDOOR_FIELD', 'INDOOR_FIELD', 'SPORTS_HALL') NOT NULL,
    capacity INT NOT NULL,
    equipment_description VARCHAR(300),
    hourly_price DECIMAL(10, 2) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE KEY uk_resource_facility_name (facility_id, name),
    CONSTRAINT fk_resource_facility FOREIGN KEY (facility_id) REFERENCES facilities(id) ON DELETE CASCADE,
    CONSTRAINT chk_resource_capacity CHECK (capacity > 0),
    CONSTRAINT chk_resource_price CHECK (hourly_price >= 0)
);

CREATE TABLE resource_sports (
    resource_id BIGINT NOT NULL,
    sport_id BIGINT NOT NULL,
    PRIMARY KEY (resource_id, sport_id),
    CONSTRAINT fk_resource_sport_resource FOREIGN KEY (resource_id) REFERENCES resources(id) ON DELETE CASCADE,
    CONSTRAINT fk_resource_sport_sport FOREIGN KEY (sport_id) REFERENCES sports(id)
);

CREATE TABLE reservations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    athlete_id BIGINT NOT NULL,
    resource_id BIGINT NOT NULL,
    sport_id BIGINT NOT NULL,
    starts_at DATETIME NOT NULL,
    ends_at DATETIME NOT NULL,
    status ENUM('PENDING', 'CONFIRMED', 'CANCELLED', 'ATTENDED', 'NO_SHOW') NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reservation_athlete FOREIGN KEY (athlete_id) REFERENCES users(id),
    CONSTRAINT fk_reservation_resource FOREIGN KEY (resource_id) REFERENCES resources(id),
    CONSTRAINT fk_reservation_sport FOREIGN KEY (sport_id) REFERENCES sports(id),
    CONSTRAINT chk_reservation_interval CHECK (ends_at > starts_at)
);

CREATE TABLE facility_reactions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    facility_id BIGINT NOT NULL,
    athlete_id BIGINT NOT NULL,
    reaction ENUM('LIKE', 'DISLIKE') NOT NULL,
    comment VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reaction_facility FOREIGN KEY (facility_id) REFERENCES facilities(id) ON DELETE CASCADE,
    CONSTRAINT fk_reaction_athlete FOREIGN KEY (athlete_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE promotions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    facility_id BIGINT NOT NULL,
    name VARCHAR(160) NOT NULL,
    starts_on DATE NOT NULL,
    ends_on DATE NOT NULL,
    discount_percent DECIMAL(5, 2),
    discount_amount DECIMAL(10, 2),
    sport_id BIGINT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_promotion_facility FOREIGN KEY (facility_id) REFERENCES facilities(id) ON DELETE CASCADE,
    CONSTRAINT fk_promotion_sport FOREIGN KEY (sport_id) REFERENCES sports(id),
    CONSTRAINT chk_promotion_dates CHECK (ends_on >= starts_on),
    CONSTRAINT chk_promotion_discount CHECK (
        (discount_percent IS NOT NULL AND discount_amount IS NULL AND discount_percent > 0 AND discount_percent <= 100)
        OR (discount_amount IS NOT NULL AND discount_percent IS NULL AND discount_amount > 0)
    )
);

CREATE TABLE facility_athlete_blocks (
    facility_id BIGINT NOT NULL,
    athlete_id BIGINT NOT NULL,
    blocked_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (facility_id, athlete_id),
    CONSTRAINT fk_block_facility FOREIGN KEY (facility_id) REFERENCES facilities(id) ON DELETE CASCADE,
    CONSTRAINT fk_block_athlete FOREIGN KEY (athlete_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE teammate_ads (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    owner_id BIGINT NOT NULL,
    sport_id BIGINT NOT NULL,
    city VARCHAR(100) NOT NULL,
    play_date DATE NOT NULL,
    play_time TIME NOT NULL,
    missing_players INT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_ad_owner FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_ad_sport FOREIGN KEY (sport_id) REFERENCES sports(id)
);

CREATE TABLE teammate_join_requests (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    ad_id BIGINT NOT NULL,
    athlete_id BIGINT NOT NULL,
    status ENUM('PENDING', 'ACCEPTED', 'REJECTED') NOT NULL DEFAULT 'PENDING',
    UNIQUE KEY uk_ad_athlete (ad_id, athlete_id),
    CONSTRAINT fk_join_ad FOREIGN KEY (ad_id) REFERENCES teammate_ads(id) ON DELETE CASCADE,
    CONSTRAINT fk_join_athlete FOREIGN KEY (athlete_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE trainers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    facility_id BIGINT NOT NULL,
    sport_id BIGINT NOT NULL,
    first_name VARCHAR(80) NOT NULL,
    last_name VARCHAR(80) NOT NULL,
    specialization VARCHAR(200) NOT NULL,
    average_rating DECIMAL(3, 2) NOT NULL DEFAULT 0,
    hourly_price DECIMAL(10, 2) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_trainer_facility FOREIGN KEY (facility_id) REFERENCES facilities(id),
    CONSTRAINT fk_trainer_sport FOREIGN KEY (sport_id) REFERENCES sports(id),
    CONSTRAINT chk_trainer_rating CHECK (average_rating >= 0 AND average_rating <= 5),
    CONSTRAINT chk_trainer_price CHECK (hourly_price >= 0)
);

CREATE TABLE individual_trainings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    athlete_id BIGINT NOT NULL,
    trainer_id BIGINT NOT NULL,
    starts_at DATETIME NOT NULL,
    ends_at DATETIME NOT NULL,
    status ENUM('SCHEDULED', 'ATTENDED', 'CANCELLED', 'NO_SHOW') NOT NULL DEFAULT 'SCHEDULED',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_training_athlete FOREIGN KEY (athlete_id) REFERENCES users(id),
    CONSTRAINT fk_training_trainer FOREIGN KEY (trainer_id) REFERENCES trainers(id),
    CONSTRAINT chk_training_interval CHECK (ends_at > starts_at)
);

CREATE TABLE equipment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    organization_id BIGINT NOT NULL,
    sport_id BIGINT NOT NULL,
    name VARCHAR(160) NOT NULL,
    image_path VARCHAR(500),
    price DECIMAL(10, 2) NOT NULL,
    stock INT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_equipment_organization_name (organization_id, name),
    CONSTRAINT fk_equipment_organization FOREIGN KEY (organization_id) REFERENCES employee_organizations(id),
    CONSTRAINT fk_equipment_sport FOREIGN KEY (sport_id) REFERENCES sports(id),
    CONSTRAINT chk_equipment_price CHECK (price >= 0),
    CONSTRAINT chk_equipment_stock CHECK (stock >= 0)
);

CREATE TABLE equipment_orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    athlete_id BIGINT NOT NULL,
    organization_id BIGINT NOT NULL,
    status ENUM('ORDERED', 'ACCEPTED', 'PICKED_UP', 'CANCELLED') NOT NULL DEFAULT 'ORDERED',
    total_amount DECIMAL(12, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_order_athlete FOREIGN KEY (athlete_id) REFERENCES users(id),
    CONSTRAINT fk_order_organization FOREIGN KEY (organization_id) REFERENCES employee_organizations(id),
    CONSTRAINT chk_order_total CHECK (total_amount >= 0)
);

CREATE TABLE equipment_order_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    equipment_id BIGINT NOT NULL,
    equipment_name VARCHAR(160) NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES equipment_orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_order_item_equipment FOREIGN KEY (equipment_id) REFERENCES equipment(id),
    CONSTRAINT chk_order_item_quantity CHECK (quantity > 0),
    CONSTRAINT chk_order_item_price CHECK (unit_price >= 0)
);

CREATE TABLE password_reset_tokens (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    token_hash CHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_password_reset_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_users_admin_listing ON users(deleted, role, username);
CREATE INDEX idx_reservation_resource_time ON reservations(resource_id, starts_at, ends_at);
CREATE INDEX idx_facility_search ON facilities(status, city, name);
CREATE INDEX idx_reaction_facility_type ON facility_reactions(facility_id, reaction);
CREATE INDEX idx_training_trainer_time ON individual_trainings(trainer_id, starts_at, ends_at);
CREATE INDEX idx_training_athlete_time ON individual_trainings(athlete_id, starts_at);
CREATE INDEX idx_equipment_catalog ON equipment(active, sport_id, name);
CREATE INDEX idx_order_athlete_created ON equipment_orders(athlete_id, created_at);
CREATE INDEX idx_order_organization_status ON equipment_orders(organization_id, status);
CREATE INDEX idx_password_reset_user_expiry ON password_reset_tokens(user_id, expires_at);

INSERT INTO sports (name) VALUES
    ('Fudbal'),
    ('Košarka'),
    ('Tenis'),
    ('Odbojka'),
    ('Plivanje');

INSERT INTO users (username, password_hash, first_name, last_name, phone, email, profile_image_path, role, status) VALUES
    ('admin', '$2b$10$GfHvN8x0qaHAuMoPogdBfOgTumecEyra63nP7EJjw/wDj42TT1q1q', 'Sistem', 'Administrator', '0000000000', 'admin@sportsphere.local', 'default-profile.png', 'ADMIN', 'ACTIVE'),
    ('sportista', '$2b$10$GfHvN8x0qaHAuMoPogdBfOgTumecEyra63nP7EJjw/wDj42TT1q1q', 'Ana', 'Jovanović', '060100100', 'sportista@sportsphere.local', 'default-profile.png', 'ATHLETE', 'ACTIVE'),
    ('zaposleni', '$2b$10$GfHvN8x0qaHAuMoPogdBfOgTumecEyra63nP7EJjw/wDj42TT1q1q', 'Milan', 'Savić', '060400400', 'zaposleni@sportsphere.local', 'default-profile.png', 'EMPLOYEE', 'ACTIVE'),
    ('anja', '$2b$10$GfHvN8x0qaHAuMoPogdBfOgTumecEyra63nP7EJjw/wDj42TT1q1q', 'Anja', 'Nikolić', '060500500', 'anja@sportsphere.local', 'default-profile.png', 'ATHLETE', 'ACTIVE');

INSERT INTO employee_organizations (name, registered_address, registration_number, tax_id) VALUES
    ('SportSphere d.o.o.', 'Bulevar sporta 1, Beograd', '11223344', '123456789');

INSERT INTO organization_employees (organization_id, employee_id) VALUES
    (1, 3);

INSERT INTO facilities (organization_id, name, city, address, description, latitude, longitude, opening_time, closing_time, allowed_no_shows, status) VALUES
    (1, 'Arena 11', 'Beograd', 'Bulevar sporta 11', 'Centar za fudbal i košarku.', 44.8125000, 20.4612000, '07:00:00', '23:00:00', 3, 'ACTIVE'),
    (1, 'Teniski centar', 'Novi Sad', 'Futoški put 25', 'Otvoreni teniski teren.', 45.2517000, 19.8369000, '08:00:00', '22:00:00', 2, 'ACTIVE'),
    (1, 'Sportski centar Čair', 'Niš', 'Park Čair 1', 'Tereni i dvorana u sportskom kompleksu Čair.', 43.3151000, 21.9133000, '07:00:00', '22:00:00', 3, 'ACTIVE'),
    (1, 'Sportski centar Jezero', 'Kragujevac', 'Grada Sirena 15', 'Centar za odbojku i rekreativne sportove.', 44.0128000, 20.9114000, '08:00:00', '22:00:00', 2, 'ACTIVE');

INSERT INTO resources (facility_id, name, type, capacity, equipment_description, hourly_price) VALUES
    (1, 'Fudbalski teren', 'OUTDOOR_FIELD', 12, 'Golovi, mreže i reflektori', 4200.00),
    (1, 'Košarkaška hala', 'SPORTS_HALL', 20, 'Koševi, semafor i svlačionice', 5500.00),
    (2, 'Teniski teren', 'OUTDOOR_FIELD', 4, 'Šljaka i osvetljenje', 2800.00),
    (3, 'Mali fudbalski teren', 'OUTDOOR_FIELD', 10, 'Veštačka trava, golovi i reflektori', 3600.00),
    (3, 'Odbojkaška dvorana', 'SPORTS_HALL', 18, 'Mreža, tribine i svlačionice', 4800.00),
    (4, 'Odbojkaški teren', 'OUTDOOR_FIELD', 12, 'Mreža i noćno osvetljenje', 3000.00);

INSERT INTO resource_sports (resource_id, sport_id) VALUES
    (1, 1),
    (2, 2),
    (3, 3),
    (4, 1),
    (5, 4),
    (6, 4);

INSERT INTO facility_images (facility_id, image_path, display_order) VALUES
    (1, '/arena-11-galerija.jpg', 1);

INSERT INTO trainers (facility_id, sport_id, first_name, last_name, specialization, average_rating, hourly_price) VALUES
    (1, 1, 'Nikola', 'Ilić', 'Fudbalska tehnika i kondicija', 4.80, 3000.00),
    (2, 3, 'Jelena', 'Savić', 'Teniska tehnika i taktika', 4.90, 3500.00),
    (1, 2, 'Marko', 'Petrović', 'Košarkaška tehnika i individualni rad', 4.70, 3200.00);

INSERT INTO equipment (organization_id, sport_id, name, price, stock) VALUES
    (1, 1, 'Fudbalska lopta', 4200.00, 12),
    (1, 3, 'Teniski reket', 8900.00, 7),
    (1, 4, 'Odbojkaška lopta', 5100.00, 9);

INSERT INTO reservations (athlete_id, resource_id, sport_id, starts_at, ends_at, status) VALUES
    (2, 1, 1, DATE_ADD(DATE_SUB(CURRENT_DATE, INTERVAL 2 DAY), INTERVAL 10 HOUR), DATE_ADD(DATE_SUB(CURRENT_DATE, INTERVAL 2 DAY), INTERVAL 11 HOUR), 'ATTENDED'),
    (2, 2, 2, DATE_ADD(DATE_ADD(CURRENT_DATE, INTERVAL 3 DAY), INTERVAL 10 HOUR), DATE_ADD(DATE_ADD(CURRENT_DATE, INTERVAL 3 DAY), INTERVAL 11 HOUR), 'CONFIRMED'),
    (2, 3, 3, DATE_ADD(DATE_ADD(CURRENT_DATE, INTERVAL 5 DAY), INTERVAL 12 HOUR), DATE_ADD(DATE_ADD(CURRENT_DATE, INTERVAL 5 DAY), INTERVAL 13 HOUR), 'PENDING'),
    (2, 5, 4, DATE_ADD(DATE_SUB(CURRENT_DATE, INTERVAL 12 DAY), INTERVAL 18 HOUR), DATE_ADD(DATE_SUB(CURRENT_DATE, INTERVAL 12 DAY), INTERVAL 19 HOUR), 'ATTENDED'),
    (4, 2, 2, DATE_ADD(DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), INTERVAL 14 HOUR), DATE_ADD(DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), INTERVAL 15 HOUR), 'CONFIRMED');

INSERT INTO facility_reactions (facility_id, athlete_id, reaction, comment) VALUES
    (1, 2, 'LIKE', 'Odlični tereni i ljubazno osoblje.');

INSERT INTO promotions (facility_id, name, starts_on, ends_on, discount_percent, sport_id) VALUES
    (1, 'Letnji fudbal', CURRENT_DATE, DATE_ADD(CURRENT_DATE, INTERVAL 60 DAY), 15.00, 1),
    (2, 'Jutarnji tenis', CURRENT_DATE, DATE_ADD(CURRENT_DATE, INTERVAL 60 DAY), 10.00, 3),
    (3, 'Večernja odbojka', CURRENT_DATE, DATE_ADD(CURRENT_DATE, INTERVAL 45 DAY), 12.00, 4);

INSERT INTO teammate_ads (owner_id, sport_id, city, play_date, play_time, missing_players, active) VALUES
    (2, 1, 'Beograd', DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY), '18:00:00', 3, TRUE),
    (2, 4, 'Niš', DATE_ADD(CURRENT_DATE, INTERVAL 20 DAY), '19:00:00', 2, TRUE);

INSERT INTO individual_trainings (athlete_id, trainer_id, starts_at, ends_at, status) VALUES
    (2, 1, DATE_ADD(DATE_SUB(CURRENT_DATE, INTERVAL 30 DAY), INTERVAL 10 HOUR), DATE_ADD(DATE_SUB(CURRENT_DATE, INTERVAL 30 DAY), INTERVAL 11 HOUR), 'ATTENDED'),
    (2, 1, DATE_ADD(DATE_ADD(CURRENT_DATE, INTERVAL 7 DAY), INTERVAL 18 HOUR), DATE_ADD(DATE_ADD(CURRENT_DATE, INTERVAL 7 DAY), INTERVAL 19 HOUR), 'SCHEDULED'),
    (2, 2, DATE_ADD(DATE_ADD(CURRENT_DATE, INTERVAL 10 DAY), INTERVAL 17 HOUR), DATE_ADD(DATE_ADD(CURRENT_DATE, INTERVAL 10 DAY), INTERVAL 18 HOUR), 'SCHEDULED');

INSERT INTO equipment_orders (athlete_id, organization_id, status, total_amount) VALUES
    (2, 1, 'ORDERED', 4200.00),
    (2, 1, 'PICKED_UP', 8900.00),
    (2, 1, 'ACCEPTED', 8400.00);

INSERT INTO equipment_order_items (order_id, equipment_id, equipment_name, quantity, unit_price) VALUES
    (1, 1, 'Fudbalska lopta', 1, 4200.00),
    (2, 2, 'Teniski reket', 1, 8900.00),
    (3, 1, 'Fudbalska lopta', 2, 4200.00);

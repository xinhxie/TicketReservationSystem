-- Delete tables if exist
DROP TABLE IF EXISTS tickets;
DROP TABLE IF EXISTS events;
DROP TYPE IF EXISTS ticket_status;

-- 1. Create the Events table
CREATE TABLE IF NOT EXISTS events (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    total_tickets INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Create the Enum Type
CREATE TYPE ticket_status AS ENUM ('AVAILABLE', 'RESERVED', 'SOLD');

-- 3. Create the Tickets table using the Enum type directly
-- Remove the 'ALTER TABLE' command later by defining it correctly here
CREATE TABLE IF NOT EXISTS tickets (
    id SERIAL PRIMARY KEY,
    event_id INTEGER REFERENCES events(id),
    status ticket_status DEFAULT 'AVAILABLE'::ticket_status,
    version INTEGER DEFAULT 0,              -- Used for Optimistic Locking
    owner_email VARCHAR(255),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 4. Seed initial data with explicit casting
INSERT INTO events (name, total_tickets) VALUES ('Concert: The Concurrency Tour', 100);

-- Insert 10 tickets for this event using the :: operator to cast strings to your Enum
INSERT INTO tickets (event_id, status, version) 
SELECT 1, 'AVAILABLE'::ticket_status, 0 FROM generate_series(1, 10);
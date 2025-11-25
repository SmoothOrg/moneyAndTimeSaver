-- PostgreSQL Database Setup Script
-- Database: money_time_saver

-- Create database (run as postgres superuser)
CREATE DATABASE money_time_saver;

-- Connect to the database
\c money_time_saver;

-- Enable UUID extension (optional, for future use)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Tables will be auto-created by Hibernate with spring.jpa.hibernate.ddl-auto=update
-- But here's the explicit schema for reference:

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    name VARCHAR(255),
    default_latitude DECIMAL(10, 8),
    default_longitude DECIMAL(11, 8),
    default_geohash VARCHAR(12),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Cart items table
CREATE TABLE IF NOT EXISTS cart_items (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    product_data JSONB NOT NULL,
    quantity INTEGER DEFAULT 1,
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- User locations table
CREATE TABLE IF NOT EXISTS user_locations (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(100),
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    geohash VARCHAR(12),
    is_default BOOLEAN DEFAULT false
);

-- Indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_cart_user_id ON cart_items(user_id);
CREATE INDEX IF NOT EXISTS idx_user_locations_user_id ON user_locations(user_id);
CREATE INDEX IF NOT EXISTS idx_cart_product_id ON cart_items USING GIN ((product_data->'product_id'));

-- Sample test user (password: password123)
INSERT INTO users (email, password_hash, name, default_latitude, default_longitude, default_geohash)
VALUES ('test@example.com', '$2a$10$XrJ9wE7LXZO7EjxB9VmXH.7Oq0EhKvL3d3kXCZ9VcN5pV5qYp5F7y', 'Test User', 28.5687, 77.1886, 'ttnt7u5p9')
ON CONFLICT (email) DO NOTHING;

COMMENT ON TABLE users IS 'User accounts with authentication and default location';
COMMENT ON TABLE cart_items IS 'Shopping cart items stored as JSONB for flexibility';
COMMENT ON TABLE user_locations IS 'Multiple saved locations per user (home, office, etc.)';

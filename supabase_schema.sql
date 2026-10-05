-- ==========================================
-- FootAuction FC: Supabase Database Schema
-- Run this script in Supabase -> SQL Editor
-- ==========================================

-- 1. Users Table (Accounts & Profiles)
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(100) PRIMARY KEY,
    email VARCHAR(255) UNIQUE,
    manager_name VARCHAR(60) NOT NULL,
    tag_number INT NOT NULL,
    full_tag VARCHAR(70) UNIQUE NOT NULL,
    club_name VARCHAR(80) NOT NULL,
    avatar_icon VARCHAR(10) DEFAULT '⚽',
    budget BIGINT DEFAULT 20000000,
    trophies INT DEFAULT 0,
    division_tier INT DEFAULT 9,
    created_at BIGINT DEFAULT EXTRACT(EPOCH FROM NOW()) * 1000
);

-- 2. Squads Table (Cloud Squad Persistence)
CREATE TABLE IF NOT EXISTS squads (
    user_id VARCHAR(100) PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    starting_11 JSONB DEFAULT '[]'::jsonb,
    reserves JSONB DEFAULT '[]'::jsonb,
    formation VARCHAR(50) DEFAULT '4-3-3 Attack',
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 3. Friendships Table (Social Network)
CREATE TABLE IF NOT EXISTS friendships (
    id SERIAL PRIMARY KEY,
    user_id VARCHAR(100) REFERENCES users(id) ON DELETE CASCADE,
    friend_id VARCHAR(100) REFERENCES users(id) ON DELETE CASCADE,
    status VARCHAR(20) DEFAULT 'ACCEPTED',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(user_id, friend_id)
);

-- 4. Friend Requests Table
CREATE TABLE IF NOT EXISTS friend_requests (
    request_id VARCHAR(100) PRIMARY KEY,
    from_user_id VARCHAR(100) REFERENCES users(id) ON DELETE CASCADE,
    to_user_id VARCHAR(100) REFERENCES users(id) ON DELETE CASCADE,
    status VARCHAR(20) DEFAULT 'PENDING',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 5. Global Transfer Market Listings Table
CREATE TABLE IF NOT EXISTS market_listings (
    id VARCHAR(100) PRIMARY KEY,
    seller_id VARCHAR(100) REFERENCES users(id) ON DELETE CASCADE,
    seller_name VARCHAR(60) NOT NULL,
    card_data JSONB NOT NULL,
    starting_bid BIGINT NOT NULL,
    current_bid BIGINT NOT NULL,
    buy_now_price BIGINT NOT NULL,
    highest_bidder_id VARCHAR(100) REFERENCES users(id),
    expires_at BIGINT NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE'
);

-- Insert Default Zidane Bot
INSERT INTO users (id, email, manager_name, tag_number, full_tag, club_name, avatar_icon, budget, trophies, division_tier)
VALUES ('usr_bot_zidane', 'zidane@footauction.fc', 'Zinedine Zidane', 1998, 'Zinedine Zidane#1998', 'Icon Legends FC', '👑', 150000000, 880, 1)
ON CONFLICT (id) DO NOTHING;

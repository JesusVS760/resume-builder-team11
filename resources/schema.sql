-- SQLite database schema

-- Regular users table - for email/password authentication
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(20) PRIMARY KEY,  -- Format: U1, U2, U3...
    email VARCHAR(255) UNIQUE NOT NULL,
    name VARCHAR(255),
    password_hash VARCHAR(255),
    email_verified BOOLEAN DEFAULT FALSE,
    text_verified BOOLEAN DEFAULT FALSE    -- For future twilio verification implementation either email or phone
);

-- OAuth users table - for google and github authentication
CREATE TABLE IF NOT EXISTS oauth_users (
    id VARCHAR(20) PRIMARY KEY,  -- Format: O1, O2, O3...
    oauth_email VARCHAR(255) NOT NULL,
    name VARCHAR(255),
    oauth_provider VARCHAR(50) NOT NULL
);

-- SQLite database schema

--Users table - handles both regular and OAuth authentication
CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255),              -- NULL for OAuth-only users
    name VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    -- OAuth fields
    oauth_provider VARCHAR(50),              -- 'google', 'github', NULL for regular users
    oauth_id VARCHAR(255),                   -- OAuth provider's user ID
    oauth_email VARCHAR(255),                -- Email from OAuth provider
    
    -- Verification fields
    email_verified BOOLEAN DEFAULT FALSE,
    verification_code VARCHAR(10),
    verification_expires DATETIME,
    
    -- Account status
    is_active BOOLEAN DEFAULT TRUE,
    last_login DATETIME
);

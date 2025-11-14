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

-- Resumes table - for storing resumes --
CREATE TABLE IF NOT EXISTS resumes (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id     INTEGER NOT NULL,
    file_name   TEXT    NOT NULL,
    file_path   TEXT    NOT NULL,
    uploaded_at TEXT    DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Tailored resumes generated from an original resume --
CREATE TABLE IF NOT EXISTS tailored_resumes (
    id                  INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id             INTEGER NOT NULL,
    resume_id           INTEGER NOT NULL,   -- FK to resumes.id
    job_title           TEXT,               -- what job this was tailored for
    job_company         TEXT,
    job_description     TEXT,               -- store the JD text you used
    tailored_text       TEXT    NOT NULL,   -- the actual tailored resume content
    file_path           TEXT,               -- exported PDF/DOCX path
    created_at          TEXT    DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id)   REFERENCES users(id)    ON DELETE CASCADE,
    FOREIGN KEY (resume_id) REFERENCES resumes(id)  ON DELETE CASCADE
);
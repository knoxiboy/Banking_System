-- ============================================================
-- Banking System - Database Schema (v2.0 - Enhanced)
-- Run this script once to set up the full database schema.
-- WARNING: This drops existing banking_system tables first.
-- ============================================================

CREATE DATABASE IF NOT EXISTS banking_system;
USE banking_system;

-- Drop old tables (order matters for foreign keys)
DROP TABLE IF EXISTS transactions;
DROP TABLE IF EXISTS accounts;
DROP TABLE IF EXISTS Accounts;   -- old table name
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS User;       -- old table name

-- ─── Table 1: users ─────────────────────────────────────────────────────────
-- Stores all registered users. Passwords are stored as SHA-256 hex hashes.
CREATE TABLE users (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    full_name   VARCHAR(100) NOT NULL,
    email       VARCHAR(100) UNIQUE NOT NULL,
    password    VARCHAR(64)  NOT NULL,           -- SHA-256 hash (64 hex chars)
    role        ENUM('USER', 'ADMIN') DEFAULT 'USER',
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ─── Table 2: accounts ──────────────────────────────────────────────────────
-- Stores bank accounts. Each user may have one account.
-- security_pin is also stored as a SHA-256 hash.
CREATE TABLE accounts (
    account_number  BIGINT PRIMARY KEY,
    email           VARCHAR(100) NOT NULL,
    full_name       VARCHAR(100) NOT NULL,
    balance         DECIMAL(15, 2) DEFAULT 0.00,
    security_pin    VARCHAR(64)  NOT NULL,       -- SHA-256 hash
    account_type    ENUM('SAVINGS', 'CURRENT') DEFAULT 'SAVINGS',
    status          ENUM('ACTIVE', 'FROZEN') DEFAULT 'ACTIVE',
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (email) REFERENCES users(email)
);

-- ─── Table 3: transactions ──────────────────────────────────────────────────
-- Full audit trail of every banking operation.
-- ref_account is populated for TRANSFER_IN / TRANSFER_OUT (the counterpart).
CREATE TABLE transactions (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    account_number  BIGINT NOT NULL,
    type            ENUM('DEBIT', 'CREDIT', 'TRANSFER_IN', 'TRANSFER_OUT') NOT NULL,
    amount          DECIMAL(15, 2) NOT NULL,
    description     VARCHAR(255),
    balance_after   DECIMAL(15, 2) NOT NULL,
    ref_account     BIGINT,                     -- counterpart account for transfers
    timestamp       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account_number) REFERENCES accounts(account_number)
);

-- ─── Verification ───────────────────────────────────────────────────────────
SELECT 'Schema created successfully!' AS Status;
SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'banking_system';

-- Note: The default admin account (admin@bank.com / Admin@123) is created
--       automatically by the application on first launch.


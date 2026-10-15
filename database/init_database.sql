-- ============================================================================
-- AcadVision Database Setup Script for pgAdmin
-- Run this in pgAdmin Query Tool connected to your local PostgreSQL server
-- ============================================================================

-- Step 1: Create application user (if not exists)
DO
$do$
BEGIN
   IF NOT EXISTS (
      SELECT FROM pg_catalog.pg_roles
      WHERE  rolname = 'acadvision') THEN
      CREATE ROLE acadvision LOGIN PASSWORD 'acadvision_dev';
   END IF;
END
$do$;

-- Step 2: Create database with acadvision as owner
-- (Note: If running as a batch, CREATE DATABASE cannot run inside a multi-statement transaction in some clients.
-- If so, run CREATE DATABASE acadvision OWNER acadvision; separately)
CREATE DATABASE acadvision OWNER acadvision;

-- Step 3: Grant all privileges
GRANT ALL PRIVILEGES ON DATABASE acadvision TO acadvision;

-- Add active column to services table for soft delete
ALTER TABLE services 
ADD COLUMN active TINYINT(1) NOT NULL DEFAULT 1 
COMMENT 'Soft delete flag: 1=active, 0=deleted';

-- Set all existing records to active
UPDATE services SET active = 1 WHERE active IS NULL;

-- Add index for better query performance
CREATE INDEX idx_services_active ON services(active);

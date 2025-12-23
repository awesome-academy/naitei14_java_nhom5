-- Add active column to applications table for soft delete
ALTER TABLE applications 
ADD COLUMN active TINYINT(1) NOT NULL DEFAULT 1 
COMMENT 'Soft delete flag: 1=active, 0=deleted';

-- Set all existing records to active
UPDATE applications SET active = 1 WHERE active IS NULL;

-- Add index for better query performance
CREATE INDEX idx_applications_active ON applications(active);

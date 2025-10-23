-- Add blob_url and storage_type columns to documents table for Azure Blob Storage support
ALTER TABLE documents 
ADD COLUMN blob_url VARCHAR(512) NULL,
ADD COLUMN storage_type VARCHAR(20) DEFAULT 'LOCAL' NULL;

-- Update existing records to have storage_type = 'LOCAL'
UPDATE documents SET storage_type = 'LOCAL' WHERE storage_type IS NULL;

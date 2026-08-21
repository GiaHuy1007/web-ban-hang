-- Add missing updated_at column to wishlists table
ALTER TABLE wishlists ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

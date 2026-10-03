-- V2__Add_Pet_Photo_And_Attributes.sql
-- Add photo and additional attributes to pets table

ALTER TABLE pets
ADD COLUMN sex VARCHAR(20),
ADD COLUMN markings TEXT,
ADD COLUMN photo_url TEXT;

-- Create index for faster queries by owner and name
CREATE INDEX idx_pets_owner_name ON pets(owner_id, name);

-- Create index for microchip searches
CREATE INDEX idx_pets_microchip ON pets(microchip_id);

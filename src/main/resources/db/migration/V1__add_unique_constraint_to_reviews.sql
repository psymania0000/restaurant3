-- First, remove any duplicate reviews (keeping the most recent one)
DELETE r1 FROM reviews r1
INNER JOIN reviews r2
WHERE r1.id < r2.id
AND r1.user_id = r2.user_id
AND r1.restaurant_id = r2.restaurant_id;

-- Add unique constraint
ALTER TABLE reviews
ADD CONSTRAINT uk_user_restaurant UNIQUE (user_id, restaurant_id); 
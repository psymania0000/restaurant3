-- 기존 카테고리 값을 enum 값으로 업데이트
UPDATE menus 
SET category = 'APPETIZER' 
WHERE category = '전채' OR category = 'appetizer' OR category = 'APPETIZER';

UPDATE menus 
SET category = 'MAIN' 
WHERE category = '메인' OR category = 'main' OR category = 'MAIN';

UPDATE menus 
SET category = 'SIDE' 
WHERE category = '사이드' OR category = 'side' OR category = 'SIDE';

UPDATE menus 
SET category = 'DESSERT' 
WHERE category = '디저트' OR category = 'dessert' OR category = 'DESSERT';

UPDATE menus 
SET category = 'BEVERAGE' 
WHERE category = '음료' OR category = 'beverage' OR category = 'BEVERAGE'; 
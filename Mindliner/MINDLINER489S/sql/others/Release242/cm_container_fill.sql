ALTER TABLE `mindliner5`.`containers` ADD COLUMN `FILL` DOUBLE NOT NULL DEFAULT 0  AFTER `COLOR` , ADD COLUMN `STROKE_WIDTH` DOUBLE NOT NULL  AFTER `FILL` , ADD COLUMN `STROKE_STYLE` VARCHAR(45) NOT NULL  AFTER `STROKE_WIDTH` ;

update mindliner5.containers set STROKE_STYLE = 'SOLID';
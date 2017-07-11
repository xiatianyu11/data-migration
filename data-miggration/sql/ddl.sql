CREATE TABLE IF NOT EXISTS `partitioningconfig` 
(	
	`partitioningId` INT(11) NOT NULL AUTO_INCREMENT,
	`tableName` VARCHAR(100) NOT NULL,
	`nodeTableName` VARCHAR(100) NOT NULL,
	`partitioningTableName` VARCHAR(100) NOT NULL,
	`partitioningGroup` VARCHAR(100) NOT NULL,
	PRIMARY KEY (`partitioningId`)
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB;



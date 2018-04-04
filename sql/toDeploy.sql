USE recordr;

# set records.Id to auto increment

SET FOREIGN_KEY_CHECKS = 0;

ALTER TABLE records
MODIFY column Id int(11) auto_increment;

ALTER TABLE containers
MODIFY column Id int(11) auto_increment;

SET FOREIGN_KEY_CHECKS = 1;

# set recordsclassifications foreign key to on delete CASCADE

ALTER TABLE recordclassifications
DROP FOREIGN KEY  `FK_RecordClassifications_Records`;

ALTER TABLE recordclassifications
ADD  CONSTRAINT `FK_RecordClassifications_Records` FOREIGN KEY (`RecordId`) REFERENCES `records` (`Id`) ON DELETE CASCADE ON UPDATE NO ACTION;

# Update recordtypes number pattern based on https://piazza.com/class/j9rznx4s44a36c?cid=323
UPDATE recordtypes SET NumberPattern='KKK-yyyy/ggg' WHERE Id = 22;
UPDATE recordtypes SET NumberPattern='KKK-XXXX-XXXX.XXX' WHERE Id = 30;
UPDATE recordtypes SET NumberPattern='KKK_P_yyyy.ggg' WHERE Id = 32;
UPDATE recordtypes SET NumberPattern='KKK-TASK-XXXX' WHERE Id = 73;
UPDATE recordtypes SET NumberPattern='nnnnzzzz.nn.a.nn.nn[:nn]' WHERE Id = 83;
UPDATE recordtypes SET NumberPattern='KKK-CLIENT.gggg' WHERE Id=70;

# Update location to have restricted flag
ALTER TABLE locations ADD Restricted BOOLEAN DEFAULT false;
UPDATE locations SET Restricted = true Where Id IN (84, 97);

# Add containers into notetables
INSERT into notetables values(79, "Containers");

# Create auditlogs table
CREATE TABLE IF NOT EXISTS `auditlogs` (
  `Id` int(11) NOT NULL AUTO_INCREMENT,
  `UserId` int(11) NOT NULL,
  `Action` ENUM('CREATE', 'UPDATE', 'DELETE') CHARACTER SET utf8mb4 NOT NULL,
  `Target` ENUM('RECORD', 'CONTAINER') CHARACTER SET utf8mb4 NOT NULL,
  `TargetId` INT(11) NOT NULL,
  `CreatedAt` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
   PRIMARY KEY (`Id`)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8

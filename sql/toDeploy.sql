USE recordr;

# set records.Id to auto increment

SET FOREIGN_KEY_CHECKS = 0;

ALTER TABLE records
MODIFY column Id int(11) auto_increment;

SET FOREIGN_KEY_CHECKS = 1;

# set recordsclassifications foreign key to on delete CASCADE

ALTER TABLE recordclassifications
DROP FOREIGN KEY  `FK_RecordClassifications_Records`;

ALTER TABLE recordclassifications
ADD  CONSTRAINT `FK_RecordClassifications_Records` FOREIGN KEY (`RecordId`) REFERENCES `records` (`Id`) ON DELETE CASCADE ON UPDATE NO ACTION

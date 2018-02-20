USE recordr;

SET FOREIGN_KEY_CHECKS = 0;

ALTER TABLE records
MODIFY column Id int(11) auto_increment;

SET FOREIGN_KEY_CHECKS = 1;
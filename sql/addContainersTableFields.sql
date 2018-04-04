# Adds StateId, TypeId, LocationId, ScheduleId and DestructionDate fields

# These fields (as well as consignmentCode) keep track of the
# common StateId, TypeId, LocationId, DestructionDate, ScheduleId, and consignmentCode that
# all records in this container have

use recordr;

ALTER TABLE containers
ADD StateId int(11),
ADD LocationId int(11),
ADD ScheduleId int(11),
ADD TypeId int(11),
ADD DestructionDate datetime(6),

ADD KEY FK_Containers_RecordStates (StateId),
ADD KEY FK_Containers_Locations (LocationId),
ADD KEY FK_Containers_Schedule (ScheduleId),
ADD KEY FK_Containers_RecordTypes (TypeId),

MODIFY ConsignmentCode varchar(50) CHARACTER SET utf8mb4,

ADD CONSTRAINT FK_Containers_RecordStates FOREIGN KEY (StateId) REFERENCES recordstates (Id) ON DELETE NO ACTION ON UPDATE NO ACTION,
ADD CONSTRAINT FK_Containers_Locations FOREIGN KEY (LocationId) REFERENCES locations (Id) ON DELETE NO ACTION ON UPDATE NO ACTION,
ADD CONSTRAINT FK_Containers_Schedule FOREIGN KEY (ScheduleId) REFERENCES retentionschedules (Id) ON DELETE NO ACTION ON UPDATE NO ACTION,
ADD CONSTRAINT FK_Containers_RecordTypes FOREIGN KEY (TypeId) REFERENCES recordtypes (Id) ON DELETE NO ACTION ON UPDATE NO ACTION;

# >>NOTE!<< these queries can take around 50 seconds each to run, you may need to adjust your timeout
# adjust connection timeout in mysql workbench by edit -> preferences -> SQL editor -> DBMS connection read timeout
UPDATE containers c INNER JOIN records r ON c.id = r.containerId SET c.stateId    = r.stateId WHERE c.id > 0;
UPDATE containers c INNER JOIN records r ON c.id = r.containerId SET c.locationId = r.locationId WHERE c.id > 0;
UPDATE containers c INNER JOIN records r ON c.id = r.containerId SET c.typeId     = r.typeId WHERE c.id > 0;
UPDATE containers c INNER JOIN records r ON c.id = r.containerId SET c.scheduleId = r.scheduleId WHERE c.id > 0;

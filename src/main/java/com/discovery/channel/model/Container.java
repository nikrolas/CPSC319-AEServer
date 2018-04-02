package com.discovery.channel.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.sql.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class Container implements Document{

    private int containerId;
    private String containerNumber;
    private String title;
    private Date createdAt;
    private Date updatedAt;
    private Date destructionDate;

    private String consignmentCode;
    private int stateId;
    private String state;
    private int locationId;
    private String locationName;
    private int scheduleId;
    private String scheduleName;
    private int scheduleYear;
    private int typeId;
    private String type;
    private String notes;
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<Integer> childRecordIds;

    public Container(int containerId,
                     String containerNumber,
                     String title,
                     String consignmentCode,
                     Date createdAt,
                     Date updatedAt,
                     int stateId,
                     int locationId,
                     int scheduleId,
                     int typeId,
                     Date destructionDate,
                     List<Integer> childRecordIds,
                     String notes) {
        this.containerId = containerId;
        this.containerNumber = containerNumber;
        this.title = title;
        this.consignmentCode = consignmentCode;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.stateId = stateId;
        this.locationId = locationId;
        this.scheduleId = scheduleId;
        this.typeId = typeId;
        this.destructionDate = destructionDate;
        this.childRecordIds = childRecordIds;
        this.notes = notes;
    }

    @JsonCreator
    public Container(@JsonProperty(value = "title", required = true) String title,
                     @JsonProperty("notes") String notes,
                     @JsonProperty(value = "records", required = true) List<Integer> records,
                     @JsonProperty(value = "locationId", required = true) int locationId,
                     @JsonProperty("stateId") int stateId) {
        this.title = title;
        this.locationId = locationId;
        this.notes = notes;
        this.childRecordIds = records;
        this.stateId = stateId;
    }
}

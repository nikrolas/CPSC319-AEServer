package com.discovery.channel.model;
import com.discovery.channel.database.RecordTypeController;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by Qiushan on 2018/1/20.
 */
@Getter
@Setter
@AllArgsConstructor
public class Record {
    // directly accessible from records table
    private int id;
    private String title;
    private String number;
    private int scheduleId;
    private int typeId;
    private String consignmentCode;
    private int stateId;
    private int containerId;
    private int locationId;
    private Date createdAt;
    private Date updatedAt;
    private Date closedAt;

    // verbose
    private String location;
    private String schedule;
    private String type;
    private String state;
    private String container;
    private int scheduleYear;
    private String classifications;
    private String notes;


    //Todo convert date to proper format
    public Record(Integer id,
                  String title,
                  String number,
                  int scheduleId,
                  int typeId,
                  String consignmentCode,
                  int stateId,
                  int containerId,
                  int locationId,
                  Date createdAt,
                  Date updatedAt,
                  Date closedAt) {
        this.id = id;
        this.title = title;
        this.number = number;
        this.scheduleId = scheduleId;
        this.typeId = typeId;
        this.consignmentCode = consignmentCode;
        this.stateId = stateId;
        this.containerId = containerId;
        this.locationId = locationId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.closedAt = closedAt;
    }

    @JsonCreator
    public Record(@JsonProperty(value = "title", required = true) String title,
                  @JsonProperty("number")String number,
                  @JsonProperty(value = "scheduleId", required = true) int scheduleId,
                  @JsonProperty(value = "typeId", required = true) int typeId,
                  @JsonProperty("consignmentCode") String consignmentCode,
                  @JsonProperty("containerId") int containerId,
                  @JsonProperty(value = "locationId", required = true) int locationId,
                  @JsonProperty(value = "classifications", required = true) String classifications,
                  @JsonProperty("notes") String notes) {
        this.title = title;
        this.number = number;
        this.scheduleId = scheduleId;
        this.typeId = typeId;
        this.consignmentCode = consignmentCode;
        this.containerId = containerId;
        this.locationId = locationId;
        this.classifications = classifications;
        this.notes = notes;
    }

    public int getId() {return id;}

    public int getScheduleId() {
        return scheduleId;
    }

    public int getTypeId() {
        return typeId;
    }

    public int getStateId() {
        return stateId;
    }

    public int getContainerId() {
        return containerId;
    }

    public int getLocationId() {
        return locationId;
    }

    public String getClassifications() {return classifications;}

    public String getNumber() {
        return number;
    }

    public String getTitle() {
        return title;
    }

    public String getConsignmentCode() {
        return consignmentCode;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setStateId(int state) {
        this.stateId = state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setContainer(String container) {
        this.container = container;
    }

    public void setScheduleYear(int scheduleYear) {
        this.scheduleYear = scheduleYear;
    }

    public void setClassifications(String classifications) {
        this.classifications = classifications;
    }


    /**
     * Validate record number format based on record type
     * @return
     */
    public boolean validateRecordNum(RecordNumber.NUMBER_PATTERN pattern) {
        return pattern.match(number);
    }

    /**
     * Validate record classifications
     * @return
     * @throws SQLException
     */
    public boolean validateClassifications() throws SQLException {
        return Classification.validateClassification(classifications);
    }
}



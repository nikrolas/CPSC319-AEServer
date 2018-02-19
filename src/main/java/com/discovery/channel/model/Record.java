package com.discovery.channel.model;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.sql.Date;

/**
 * Created by Qiushan on 2018/1/20.
 */
@Getter
@Setter
@AllArgsConstructor
public class Record {

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

    private String location;
    private String schedule;

    private String type;
    private String state;
    private String container;
    private int scheduleYear;

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

    public void setLocation(String location) {
        this.location = location;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    public void setType(String type) {
        this.type = type;
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

}



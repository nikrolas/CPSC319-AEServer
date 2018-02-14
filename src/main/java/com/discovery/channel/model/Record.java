package com.discovery.channel.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

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
    private String scheduleYear;

    //Todo convert date to proper format

    public Record(ResultSet result) throws SQLException{

        this.id = result.getInt("id");
        this.title = result.getString("Title");
        this.number = result.getString("Number");
        this.scheduleId = result.getInt("ScheduleId");
        this.typeId = result.getInt("TypeId");
        this.consignmentCode = result.getString("ConsignmentCode");
        this.stateId = result.getInt("StateId");
        this.containerId = result.getInt("ContainerId");
        this.locationId = result.getInt("LocationId");
        this.createdAt = result.getDate("CreatedAt");
        this.updatedAt = result.getDate("UpdatedAt");
        this.closedAt = result.getDate("ClosedAt");

    }

    public Record(ResultSet result, String scheduleName, String scheduleYear,String typeName, String stateName, String containerName, String locationName) throws SQLException{

        this.state = stateName;
        this.container = containerName;
        this.location = locationName;
        this.schedule = scheduleName;
        this.scheduleYear = scheduleYear;
        this.type = typeName;

        this.id = result.getInt("id");
        this.title = result.getString("Title");
        this.number = result.getString("Number");
        this.scheduleId = result.getInt("ScheduleId");
        this.typeId = result.getInt("TypeId");
        this.consignmentCode = result.getString("ConsignmentCode");
        this.stateId = result.getInt("StateId");
        this.containerId = result.getInt("ContainerId");
        this.locationId = result.getInt("LocationId");
        this.createdAt = result.getDate("CreatedAt");
        this.updatedAt = result.getDate("UpdatedAt");
        this.closedAt = result.getDate("ClosedAt");


    }

}



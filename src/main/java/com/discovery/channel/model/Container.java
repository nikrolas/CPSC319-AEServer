package com.discovery.channel.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.sql.Date;
import java.util.List;

@AllArgsConstructor
public class Container {

    @Getter private int id;
    @Getter private String number;
    @Getter private String title;
    @Getter private Date createdAt;
    @Getter private Date updatedAt;
    @Getter private List<Integer> childRecordIds;

    private String consignmentCode;
    private int stateId;
    private String stateName;
    private int locationId;
    private String locationName;
    private int scheduleId;
    private String scheduleName;
    private int type;

    public Container(@NonNull int id,
                     @NonNull String number,
                     @NonNull String title,
                     @NonNull Date createdAt,
                     @NonNull Date updatedAt,
                     List<Integer> childRecordIds){
        this.id = id;
        this.number = number;
        this.title = title;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.childRecordIds = childRecordIds;
    }

}

package com.discovery.channel.form;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class UpdateRecordForm {
    private String title;
    private int scheduleId;
    private String classifications;
    private String consignmentCode;
    private String notes;
    private int stateId;
    private int containerId;

    @JsonCreator
    public UpdateRecordForm(@JsonProperty("title") String title,
                            @JsonProperty("scheduleId") int scheduleId,
                            @JsonProperty("classifications") String classifications,
                            @JsonProperty("consignmentCode") String consignmentCode,
                            @JsonProperty("notes") String notes,
                            @JsonProperty("stateId") int stateId,
                            @JsonProperty("containerId") int containerId) {
        this.title = title;
        this.scheduleId = scheduleId;
        this.classifications = classifications;
        this.consignmentCode = consignmentCode;
        this.notes = notes;
        this.stateId = stateId;
        this.containerId = containerId;
    }
}

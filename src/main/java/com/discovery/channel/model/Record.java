package com.discovery.channel.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Qiushan on 2018/1/20.
 */
public class Record {
    @JsonProperty("id")
    private int id;
    @JsonProperty("title")
    private String title;

    public Record(int id, String title) {
        this.id = id;
        this.title = title;
    }
}

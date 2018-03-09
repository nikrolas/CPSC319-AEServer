package com.discovery.channel.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class User {

    private int id;
    private String userId;
    private String firstName;
    private String lastName;

    private int roleId;
    private String role;

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<Location> locations = new ArrayList<>();

    public User(Integer id, String userId, String firstName, String lastName) {
        this.id = id;
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
    }


}

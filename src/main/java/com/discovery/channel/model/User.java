package com.discovery.channel.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class User {

    private int id;
    private String userId;
    private String firstName;
    private String lastName;

    private int roleId;
    private String role;

    private int locationId;
    private String location;

    public User(Integer id, String userId, String firstName, String lastName) {
        this.id = id;
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;

    }


}

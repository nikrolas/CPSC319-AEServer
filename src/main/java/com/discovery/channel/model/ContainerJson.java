package com.discovery.channel.model;

import lombok.Getter;

import java.util.List;

public class ContainerJson {

    @Getter private String title;
    @Getter private String number;
    @Getter private List<Integer> records;
}

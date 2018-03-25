package com.discovery.channel.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PagedResults<T> {
    private int page;
    private int pageCount;
    private List<T> results;
}

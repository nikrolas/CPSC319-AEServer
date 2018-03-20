package com.discovery.channel.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class BatchResponse {
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    List<Response> responseList = new ArrayList<>();

    @AllArgsConstructor
    @Getter
    @Setter
    public class Response {
        private int id;
        private String msg;
        private boolean status;
    }

    public void addResponse(int id, String msg, boolean status) {
        responseList.add(new Response(id, msg, status));
    }
}

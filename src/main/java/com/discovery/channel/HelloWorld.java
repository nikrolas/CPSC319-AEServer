package com.discovery.channel;

import com.discovery.channel.database.DBUtil;
import com.discovery.channel.model.Record;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/hello")
public class HelloWorld {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getMessage(){
        return "Hello world!";
    }

    @GET
    @Path("/hello")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRecordByRid(){
        Record record = DBUtil.getRecordById(80);
        return Response.
                ok(record, MediaType.APPLICATION_JSON).
                build();
    }
}

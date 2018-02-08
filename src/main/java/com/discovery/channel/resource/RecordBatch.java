package com.discovery.channel.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by Qiushan on 2018/1/17.
 */
@Path("messages")
public class RecordBatch {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRecords(){

        return Response.ok("List Retrieved", MediaType.APPLICATION_JSON).build();
    }
}

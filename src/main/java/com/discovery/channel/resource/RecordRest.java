package com.discovery.channel.resource;

import com.discovery.channel.database.DBUtil;
import com.discovery.channel.model.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by Qiushan on 2018/1/17.
 */
@Path("record")
public class RecordRest {
    private static final Logger LOGGER = LoggerFactory.getLogger(RecordRest.class);
    @GET
    @Path("{rid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRecordByRid(@PathParam("rid") Integer rid){
        LOGGER.info("Received request : Getting record Id {}", rid);
        Record record = DBUtil.getRecordById(rid);
        return Response.
                ok(record, MediaType.APPLICATION_JSON).
                build();
    }
}

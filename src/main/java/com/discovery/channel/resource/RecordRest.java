package com.discovery.channel.resource;

import com.discovery.channel.database.DBUtil;
import com.discovery.channel.model.Record;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.SQLException;

/**
 * Created by Qiushan on 2018/1/17.
 */
//@Path("record")
public class RecordRest {
//    @GET
//    @Path("{rid}")
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response getRecordByRid(@PathParam("rid") Integer rid){
//        Record record = DBUtil.getRecordById(rid);
//        return Response.
//                ok(record, MediaType.APPLICATION_JSON).
//                build();
//    }

}

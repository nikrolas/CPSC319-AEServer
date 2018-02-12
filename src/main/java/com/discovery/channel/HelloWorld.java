package com.discovery.channel;

import com.discovery.channel.database.DBUtil;
import com.discovery.channel.model.Record;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@RestController
public class HelloWorld {

    /*@RequestMapping(value = "/hello", method = RequestMethod.GET)
    public Record record(@RequestParam(value="id", defaultValue="80") int id) {
        System.out.println(id);
        Record recordResponse = new Record(id, "My cool title.");
        System.out.println(recordResponse);
        return recordResponse;
    }

    /*@GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getMessage(){
        return "Hello world!";
    }*/

    //@GET
    //@Path("/hello")
    @RequestMapping(value = "/hello", method = RequestMethod.GET)
    @Produces(MediaType.APPLICATION_JSON)
    public Record getRecordByRid(){
        Record record = DBUtil.getRecordById(80);
        //List records = new ArrayList();
        //records.add(record);
        //return (Record[]) records.toArray(new Record[records.size()]);
        return record;
        /*return Response.
                ok(record, MediaType.APPLICATION_JSON).
                build();*/
    }

    @RequestMapping(value = "/goodbye", method = RequestMethod.GET)
    @Produces(MediaType.APPLICATION_JSON)
    public Record goodbye() {
        return new Record(1, "The first record.");
    }
}

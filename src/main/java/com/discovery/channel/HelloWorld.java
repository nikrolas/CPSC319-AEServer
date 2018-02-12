package com.discovery.channel;

import com.discovery.channel.database.DBUtil;
import com.discovery.channel.model.Record;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;

@RestController
public class HelloWorld {


    @RequestMapping(
            value = "records/{id}",
            params = {"userId"},
            method = RequestMethod.GET)
    public Record getRecordById(@PathVariable("id") Integer id,
                                @RequestParam("userId") int userId) throws SQLException {

        return DBUtil.getRecordById(id);
    }


    @RequestMapping(
            value = "records/{id}",
            params = {"userId"},
            method = RequestMethod.PUT)
    public int updateOneRecord(@RequestParam("userId") int userId){
        return 1;
    }

    @RequestMapping(
            value = "records",
            params = { "userId"},
            method = RequestMethod.GET)
    @ResponseBody
    public List<Record> getAllRecords(@RequestParam("userId") int userId) throws SQLException{

        return DBUtil.getAllRecords();

    }

    @RequestMapping(
            value = "records",
            params = { "userId" , "num"},
            method = RequestMethod.GET)
    @ResponseBody
    public List<Record> searchRecordsByNumber(@RequestParam("userId") int userId,
                                      @RequestParam("num") String num) throws SQLException{

        return DBUtil.getRecordByNumber(num);

    }




}

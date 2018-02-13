package com.discovery.channel.rest;

import com.discovery.channel.database.RecordController;
import com.discovery.channel.model.Record;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;

@RestController
public class RouteHandler {

    private RecordController database;

    public RouteHandler(){
        this.database = new RecordController();
    }


    /**
     * Single record get by ID
     *
     * @param  id
     * @return a single record
     */
    @RequestMapping(
            value = "records/{id}",
            params = {"userId"},
            method = RequestMethod.GET)
    public Record getRecordById(@PathVariable("id") Integer id,
                                @RequestParam("userId") int userId) throws SQLException {

        return database.getRecordById(id);
    }


    /**
     * Retrieve all records
     *
     * @param
     * @return a list of records, currently limit 20 order by UpdatedAt
     */
    @RequestMapping(
            value = "records",
            params = { "userId"},
            method = RequestMethod.GET)
    @ResponseBody
    public List<Record> getAllRecords(@RequestParam("userId") int userId) throws SQLException{

        return database.getAllRecords();

    }


    /**
     * Search records by record number
     *
     * @param  num
     * @return a list of records filtered by search content
     */
    @RequestMapping(
            value = "records",
            params = { "userId" , "num"},
            method = RequestMethod.GET)
    @ResponseBody
    public List<Record> searchRecordsByNumber(@RequestParam("userId") int userId,
                                      @RequestParam("num") String num) throws SQLException{

        return database.getRecordByNumber(num);

    }

    /**
     * Update a record by record id
     *
     * @param  id
     * @return a list of records filtered by search content
     */
    @RequestMapping(
            value = "records/{id}",
            params = {"userId"},
            method = RequestMethod.PUT)
    public int updateOneRecord(@PathVariable("id") Integer id, @RequestParam("userId") int userId){
        return 1;
    }



}

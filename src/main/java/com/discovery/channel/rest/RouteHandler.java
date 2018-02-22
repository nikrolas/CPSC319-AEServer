package com.discovery.channel.rest;

import com.discovery.channel.database.ContainerController;
import com.discovery.channel.database.RecordController;
import com.discovery.channel.model.Container;
import com.discovery.channel.model.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;

@RestController
public class RouteHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(RouteHandler.class);
    public RouteHandler(){
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
        return RecordController.getRecordById(id);
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
        return RecordController.getAllRecords();

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
        return RecordController.getRecordByNumber(num);

    }

    /**
     * Create a record
     *
     * @param
     * @return record created
     */
    @RequestMapping(
            value = "record",
            params = {"userId"},
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public ResponseEntity<Record> createRecord(@RequestParam("userId") int userId,
                                               @RequestBody Record record) throws SQLException {
        return new ResponseEntity(RecordController.createRecord(record, userId), HttpStatus.CREATED);
    }

    /**
     * Delete a record
     *
     * @param  id
     * @return a list of records filtered by search content
     */
    @RequestMapping(
            value = "record/{id}",
            params = {"userId"},
            method = RequestMethod.DELETE)
    public boolean deleteRecord (@PathVariable("id") Integer id, @RequestParam("userId") int userId) throws SQLException {
        return RecordController.deleteRecord(id, userId);
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
        //Todo continue on updating a record
        return 1;
    }

    /**
     * Get a container by id
     *
     * @param  id
     * @return the container with the given id
     */
    @RequestMapping(
            value = "containers/{id}",
            params = {"userId"},
            method = RequestMethod.GET)
    public Container getContainerById(@PathVariable("id") Integer id, @RequestParam("userId") int userId) throws SQLException{
        LOGGER.info("Searching for container with id {}", id);
        return ContainerController.getContainerById(id);
    }
}

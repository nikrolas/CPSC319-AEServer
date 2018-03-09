package com.discovery.channel.rest;

import com.discovery.channel.audit.AuditLogEntry;
import com.discovery.channel.audit.AuditLogger;
import com.discovery.channel.database.*;
import com.discovery.channel.form.UpdateRecordForm;
import com.discovery.channel.model.*;

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

    //START OF Facilitating endpoints for creating records

    /**
     * Get all root classifications
     * @return
     * @throws SQLException
     */
    @RequestMapping(
            value = "classifications",
            method = RequestMethod.GET)
    @ResponseBody
    public List<Classification> getRootClassifications() throws SQLException{
        return ClassificationController.getRootClassifications();

    }

    /**
     * Get all valid child classifications of the specified parent id
     */
    @RequestMapping(
            value = "classifications",
            params = {"parentId"},
            method = RequestMethod.GET)
    public List<Classification> getChildClassifications(@RequestParam("parentId") int parentId) throws SQLException {
        return ClassificationController.findChildrenClassifications(parentId);
    }

    /**
     * Get all record types
     * @return
     * @throws SQLException
     */
    @RequestMapping(
            value = "recordtypes",
            method = RequestMethod.GET)
    @ResponseBody
    public List<RecordType> getAllRecordTypes() throws SQLException{
        return RecordTypeController.getAllRecordTypes();

    }

    /**
     * Get all retention schedules
     * @return
     * @throws SQLException
     */
    @RequestMapping(
            value = "retentionschedules",
            method = RequestMethod.GET)
    @ResponseBody
    public List<RetentionSchedule> getAllRententionSchedules() throws SQLException{
        return RetentionScheduleController.getAllRetentionSchedules();

    }

    /**
     * Get all record states
     * @return
     * @throws SQLException
     */
    @RequestMapping(
            value = "recordstates",
            method = RequestMethod.GET)
    @ResponseBody
    public List<State> getAllRecordStates() throws SQLException{
        return StateController.getAllStates();

    }


    // END OF Facilitating endpoints for creating records

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
            method = RequestMethod.PUT,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Record updateOneRecord(@PathVariable("id") Integer id, @RequestParam("userId") int userId,  @RequestBody UpdateRecordForm updateForm) throws SQLException {
        RecordController.updateRecord(id, userId, updateForm);
        return RecordController.getRecordById(id);
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

    /**
     * Create a container
     *
     * @return the newly created container
     */
    @RequestMapping(
            value = "container",
            params = {"userId"},
            method = RequestMethod.POST)
    public ResponseEntity<Container> createContainer(@RequestParam("userId") int userId,
                                                     @RequestBody Container container)  throws SQLException{
        return new ResponseEntity<>(ContainerController.createContainer(container, userId), HttpStatus.CREATED);
    }

    /**
     * Get a user by id in user table
     *
     * @param  id
     * @return the user with the given user id
     */
    @RequestMapping(
            value = "users/{id}",
            method = RequestMethod.GET)
    public User getUserByUserTableId(@PathVariable("id") Integer id) throws SQLException{
        return UserController.getUserByUserTableId(id);

    }

    /**
     * Get audit logs
     */
    @RequestMapping(
            value = "auditlogs",
            method = RequestMethod.GET)
    public List<AuditLogEntry> getUserByUserTableId() throws SQLException{
        return AuditLogger.getLogs();

    }
}

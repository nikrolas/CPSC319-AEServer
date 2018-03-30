package com.discovery.channel.rest;

import com.discovery.channel.audit.AuditLogEntry;
import com.discovery.channel.audit.AuditLogger;
import com.discovery.channel.database.*;
import com.discovery.channel.form.ContainersForm;
import com.discovery.channel.form.RecordsForm;
import com.discovery.channel.form.UpdateRecordForm;
import com.discovery.channel.model.*;

import com.discovery.channel.response.BatchResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.ArrayList;
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
        return RecordController.getRecordById(id, userId);
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
        return RecordController.getAllRecords(userId);

    }


    /**
     * Search records by record number
     *
     * @param  num
     * @return a list of records filtered by search content
     */
    @RequestMapping(
            value = "search",
            params = { "userId" , "num", "record", "container", "page", "pageSize"},
            method = RequestMethod.GET)
    @ResponseBody

    public PagedResults<Document> searchByNumber(@RequestParam("userId") int userId,
                                              @RequestParam("num") String num,
                                              @RequestParam(value="record", required=false, defaultValue="false") Boolean record,
                                              @RequestParam(value="container", required=false, defaultValue="false") Boolean container,
                                              @RequestParam(value="page", required=false, defaultValue="1") int page,
                                              @RequestParam(value="pageSize", required=false, defaultValue="20") int pageSize)
                                              throws SQLException{
        return RecordController.getByNumber(num, record, container, page, pageSize, userId);
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

    @RequestMapping(
            value = "records",
            params = {"userId"},
            method = RequestMethod.DELETE)
    public BatchResponse deleteRecords(@RequestParam("userId") int userId, @RequestBody RecordsForm form) throws SQLException {
        return RecordController.deleteRecords(userId, form);
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
        return RecordController.getRecordById(id, userId);
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
        return ContainerController.getContainerById(id, userId);
    }

    /**
     * Create a container
     *
     * @return the newly created container
     */
    @RequestMapping(
            value = "container",
            params = {"userId"},
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Container> createContainer(@RequestParam("userId") int userId,
                                                     @RequestBody Container container)  throws SQLException{
        return new ResponseEntity<>(ContainerController.createContainer(container, userId), HttpStatus.CREATED);
    }

    /**
     * Update a container
     *
     * @return the newly updated container
     */
    @RequestMapping(
            value = "container/{id}",
            params = {"userId"},
            method = RequestMethod.PUT)
    public ResponseEntity<Container> updateContainer(@PathVariable("id") Integer id,
                                                     @RequestParam("userId") int userId,
                                                     @RequestBody Container updatedFields)  throws SQLException{
        return new ResponseEntity<>(ContainerController.updateContainer(id, updatedFields, userId), HttpStatus.OK);
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
     * Delete container(s)
     *
     * @return  a list of container(s) that can't be deleted
     */
    @RequestMapping(
            value = "containers",
            params = {"userId"},
            method = RequestMethod.DELETE)

    public ResponseEntity<?> deleteContainers(@RequestParam("userId") int userId, @RequestBody ContainersForm form) throws SQLException{
        LOGGER.info("Deleting container(s) {}", form.getContainerIds());
        return ContainerController.deleteContainers(form.getContainerIds(), userId);
    }



    /**
     * Get a destruction date given record ids
     *
     * @param  form
     * @return Destruction date in millisecond if success, error message otherwise
     */
    @RequestMapping(
            value = "destructiondate",
            params = {"userId"},
            method = RequestMethod.GET)
    public ResponseEntity<?> getDestructionDate(@RequestParam("userId") int userId, @RequestBody RecordsForm form) throws SQLException{
        LOGGER.info("Calculating destruction date given ids {}", form.getRecordIds());
        return DestructionDateController.calculateDestructionDate(form.getRecordIds());
    }

    /**
     * Create a new volume
     * @param id
     * @return
     */
    @RequestMapping(
            value = "volume/{id}",
            params = {"copyNotes", "userId"},
            method = RequestMethod.POST)
    public Record createVolume(@PathVariable("id") Integer id, @RequestParam("userId") int userId,
                                  @RequestParam("copyNotes") Boolean copyNotes) throws SQLException{
        return RecordController.createVolume(id, userId, copyNotes);
    }

    /**
     * Search for volumes related to a record Number
     *
     * @return a list of volumes matching the given record Number
     */
    @RequestMapping(
            value = "volume",
            params = {"num", "userId"},
            method = RequestMethod.GET)
    public List<Record> getVolumesByNumber(@RequestParam("num") String num,
                                           @RequestParam("userId") int userId) throws SQLException {
        LOGGER.info("Searching volumes related to number {}", num);
        return RecordController.getVolumesByNumber(num, userId);
    }

    /**
     * Get audit logs
     */
    @RequestMapping(
            value = "auditlogs",
            method = RequestMethod.GET)
    public List<AuditLogEntry> getAuditLogs() throws SQLException{
        return AuditLogger.getLogs();
    }

    /**
     * Destroy records given record ids
     *
     * @param  form
     * @return httpstatus 200 if success, error message with record id(s) that can be destroyed otherwise
     */
    @RequestMapping(
            value = "destroyrecords",
            params = {"userId"},
            method = RequestMethod.PUT)
    public ResponseEntity<?> destroyRecords(@RequestParam("userId") int userId, @RequestBody RecordsForm form) throws SQLException {
        return RecordController.prepareToDestroyRecords(form, userId);
    }



    /**
     * Get the most recent ClosedAt given a container id
     *
     * @param  containerId
     * @return httpstatus 200 if success, error message with record(s) that don't not have ClosedAt
     */
    @RequestMapping(
            value = "container/{id}/closedAt",
            params = {"userId"},
            method = RequestMethod.GET)
    public ResponseEntity<?> getTheMostRecentClosedAt(@PathVariable("id") Integer containerId, @RequestParam("userId") int userId) throws SQLException {
        return DestructionDateController.getTheMostRecentClosedAt(containerId, userId);
    }

    /**
     * Get records given record ids
     *
     * @param  ids
     * @return list of records
     */
    @RequestMapping(
            value = "records",
            params = {"ids","userId"},
            method = RequestMethod.GET)
    public List<Record> getMultipleRecords(@RequestParam("ids") List<Integer> ids, @RequestParam("userId") int userId) throws SQLException {
        return RecordController.getRecordsByIds(ids, true);
    }

    /**
     * Get containers given container ids
     *
     * @param  ids
     * @return list of containers
     */
    @RequestMapping(
            value = "containers",
            params = {"ids","userId"},
            method = RequestMethod.GET)
    public List<Container> getMultipleContainers(@RequestParam("ids") List<Integer> ids, @RequestParam("userId") int userId) throws SQLException {
        return ContainerController.getContainersByIds(ids);
    }

}

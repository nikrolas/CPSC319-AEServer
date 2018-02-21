package com.discovery.channel.database;

public class NoResultsFoundException extends RuntimeException {

    public NoResultsFoundException(String s){
        super(s);
    }
}

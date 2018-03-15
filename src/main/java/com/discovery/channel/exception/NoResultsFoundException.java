package com.discovery.channel.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class NoResultsFoundException extends RuntimeException {

    public NoResultsFoundException(String s){
        super(s);
    }
}

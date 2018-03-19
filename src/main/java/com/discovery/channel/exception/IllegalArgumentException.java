package com.discovery.channel.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class IllegalArgumentException extends RuntimeException{
    public IllegalArgumentException(String s) {super(s);}
}

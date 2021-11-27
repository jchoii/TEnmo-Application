package com.techelevator.tenmo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class TransferNotFoundException extends Throwable {

    public TransferNotFoundException() {
        super("No matching transfer was found.");
    }
}

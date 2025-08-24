package com.example.file_uploader.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class FileAlreadyUploadedException extends RuntimeException {

    private final HttpStatus httpStatus;

    public FileAlreadyUploadedException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

}

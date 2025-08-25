package com.example.file_uploader.controller;

import com.example.file_uploader.dto.ErrorResponseDto;
import com.example.file_uploader.exception.FileAlreadyUploadedException;
import com.example.file_uploader.exception.UnsupportedFileException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponseDto> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException e) {
        ErrorResponseDto error = new ErrorResponseDto(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        return new ResponseEntity<>(error, error.getHttpStatus());
    }

    @ExceptionHandler(UnsupportedFileException.class)
    public ResponseEntity<ErrorResponseDto> handleUnsupportedFile(UnsupportedFileException e) {
        ErrorResponseDto error = new ErrorResponseDto(e.getHttpStatus(), e.getMessage());
        return new ResponseEntity<>(error, error.getHttpStatus());
    }

    @ExceptionHandler(FileAlreadyUploadedException.class)
    public ResponseEntity<ErrorResponseDto> handleFileAlreadyUploaded(FileAlreadyUploadedException e) {
        ErrorResponseDto error = new ErrorResponseDto(e.getHttpStatus(), e.getMessage());
        return new ResponseEntity<>(error, error.getHttpStatus());
    }

}

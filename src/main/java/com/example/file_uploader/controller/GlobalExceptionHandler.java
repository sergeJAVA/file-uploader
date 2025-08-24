package com.example.file_uploader.controller;

import com.example.file_uploader.dto.ErrorResponseDto;
import com.example.file_uploader.exception.FileAlreadyUploadedException;
import com.example.file_uploader.exception.UnsupportedFileException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ErrorResponseDto handleMaxUploadSizeExceeded(MaxUploadSizeExceededException e) {
        return new ErrorResponseDto(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }

    @ExceptionHandler(UnsupportedFileException.class)
    public ErrorResponseDto handleUnsupportedFile(UnsupportedFileException e) {
        return new ErrorResponseDto(e.getHttpStatus(), e.getMessage());
    }

    @ExceptionHandler(FileAlreadyUploadedException.class)
    public ErrorResponseDto handleFileAlreadyUploaded(FileAlreadyUploadedException e) {
        return new ErrorResponseDto(e.getHttpStatus(), e.getMessage());
    }

}

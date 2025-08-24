package com.example.file_uploader.service;

import com.example.file_uploader.dto.ResponseDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileUploadService {

    ResponseDto upload(MultipartFile file) throws Exception;

}

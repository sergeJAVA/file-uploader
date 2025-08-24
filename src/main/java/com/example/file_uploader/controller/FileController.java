package com.example.file_uploader.controller;

import com.example.file_uploader.dto.ResponseDto;
import com.example.file_uploader.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class FileController {

    private final FileUploadService fileUploadService;

    @PostMapping("/upload")
    public ResponseDto upload(@RequestParam MultipartFile file) throws Exception {
        return fileUploadService.upload(file);
    }

}

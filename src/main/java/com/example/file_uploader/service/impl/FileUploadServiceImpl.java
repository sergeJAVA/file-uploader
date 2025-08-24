package com.example.file_uploader.service.impl;

import com.example.file_uploader.constant.FileStatus;
import com.example.file_uploader.dto.CheckStatusRequest;
import com.example.file_uploader.dto.FileDto;
import com.example.file_uploader.dto.ResponseDto;
import com.example.file_uploader.exception.FileAlreadyUploadedException;
import com.example.file_uploader.exception.UnsupportedFileException;
import com.example.file_uploader.service.FileUploadService;
import com.example.file_uploader.service.feign.FileStatusProcessor;
import com.example.file_uploader.util.FileHashUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


@Service
@RequiredArgsConstructor
public class FileUploadServiceImpl implements FileUploadService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final FileStatusProcessor fileStatusProcessor;
    private final ObjectMapper objectMapper;

    @SneakyThrows
    @Override
    public ResponseDto upload(MultipartFile file) {

        if (file.isEmpty()) {
            throw new UnsupportedFileException(
                    "The file is empty",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }

        byte[] fileBytes = file.getBytes();
        String checksum = FileHashUtils.calculateHash(fileBytes);

        if (!isFileUploaded(checksum)) {
            fileStatusProcessor.saveFile(FileDto.builder()
                            .checksum(checksum)
                            .fileBytes(fileBytes)
                            .fileName(file.getOriginalFilename())
                            .fileStatus(FileStatus.FILE_ACCEPTED)
                            .build()
            );
        } else {
            throw new FileAlreadyUploadedException(
                    "This file has already been uploaded.",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

        if (isValidFile(file.getOriginalFilename())) {
            FileDto fileDtoSuccess = createFileDtoSuccess(file.getOriginalFilename(), fileBytes, checksum);
            kafkaTemplate.send("upload", objectMapper.writeValueAsString(fileDtoSuccess));
            kafkaTemplate.send("status", objectMapper.writeValueAsString(fileDtoSuccess));

            return new ResponseDto(HttpStatus.OK, checksum);
        } else {
            kafkaTemplate.send("status",
                    objectMapper.writeValueAsString(createFileDtoFailure(file.getOriginalFilename(), fileBytes, checksum)));
            throw new UnsupportedFileException(
                    "The file extension is not supported",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }

    }

    private boolean isValidFile(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return false;
        }
        int startExtensionIndex = fileName.lastIndexOf('.');
        String fileExtension = fileName.substring(startExtensionIndex);
        return ".xls".equals(fileExtension) || ".xlsx".equals(fileExtension);
    }

    private boolean isFileUploaded(String checkSum) {
        FileDto fileDto = fileStatusProcessor.checkStatus(checkSum);
        if (fileDto != null) {
            return fileDto.getFileStatus().equals(FileStatus.FILE_UPLOADED);
        }
        return false;
    }

    private FileDto createFileDtoSuccess(String fileName, byte[] fileBytes, String checksum) {
        return FileDto.builder()
                .checksum(checksum)
                .fileName(fileName)
                .fileStatus(FileStatus.FIRST_VALIDATION_SUCCESS)
                .fileBytes(fileBytes)
                .build();
    }

    private FileDto createFileDtoFailure(String fileName, byte[] fileBytes, String checksum) {
        return FileDto.builder()
                .checksum(checksum)
                .fileName(fileName)
                .fileStatus(FileStatus.FIRST_VALIDATION_FAILURE)
                .fileBytes(fileBytes)
                .build();
    }

}

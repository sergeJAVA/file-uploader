package com.example.file_uploader.service.impl;

import com.example.file_uploader.constant.FileStatus;
import com.example.file_uploader.dto.FileDto;
import com.example.file_uploader.dto.ResponseDto;
import com.example.file_uploader.exception.FileAlreadyUploadedException;
import com.example.file_uploader.exception.UnsupportedFileException;
import com.example.file_uploader.service.feign.FileStatusProcessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.MediaType;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileUploadServiceImplTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private FileStatusProcessor fileStatusProcessor;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private FileUploadServiceImpl fileUploadService;

    private MultipartFile testFile;
    private MultipartFile emptyFile;
    private MultipartFile notValidFile;
    private String fileName = "test.xlsx";

    @BeforeEach
    void setUp() {
        emptyFile = new MockMultipartFile(
                "file",
                fileName,
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                new byte[0]
        );

        testFile = new MockMultipartFile(
                "file",
                fileName,
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "excel-content".getBytes()
        );

        notValidFile = new MockMultipartFile(
                "file",
                "test.txt",
                MediaType.TEXT_PLAIN.toString(),
                "excel-content".getBytes()
        );
    }

    @Test
    void upload_EmptyFile_ThrowException() {
        assertThrows(UnsupportedFileException.class, () -> fileUploadService.upload(emptyFile));
        verifyNoMoreInteractions(fileStatusProcessor, kafkaTemplate, objectMapper);
    }

    @Test
    void upload_FileAlreadyUploaded_ThrowsException() {
        FileDto fileDto = FileDto.builder()
                .fileName("testFile")
                .fileBytes(new byte[0])
                .fileStatus(FileStatus.FILE_UPLOADED)
                .checksum("TestChecksum")
                .build();

        when(fileStatusProcessor.checkStatus(anyString())).thenReturn(fileDto);

        assertThrows(FileAlreadyUploadedException.class, () -> fileUploadService.upload(testFile));
        verify(fileStatusProcessor, times(1)).checkStatus(anyString());
        verifyNoMoreInteractions(fileStatusProcessor, kafkaTemplate);
    }

    @Test
    void upload_InvalidFileExtension_ThrowsException() throws Exception {
        when(fileStatusProcessor.checkStatus(anyString())).thenReturn(null);
        when(objectMapper.writeValueAsString(any(FileDto.class))).thenReturn("json_failure");

        UnsupportedFileException thrown = assertThrows(
                UnsupportedFileException.class,
                () -> fileUploadService.upload(notValidFile)
        );
        assertEquals("The file extension is not supported", thrown.getMessage());

        verify(fileStatusProcessor, times(1)).checkStatus(anyString());
        verify(fileStatusProcessor, times(1)).saveFile(any(FileDto.class));
        verify(kafkaTemplate, times(1)).send(eq("status"), anyString());
        verify(kafkaTemplate, never()).send(eq("upload"), anyString());
    }

    @Test
    void upload_ReturnsResponseDto_Success() throws Exception {
        when(fileStatusProcessor.checkStatus(anyString())).thenReturn(null);
        when(objectMapper.writeValueAsString(any(FileDto.class))).thenReturn("json_success");

        ResponseDto response = fileUploadService.upload(testFile);

        verify(fileStatusProcessor, times(1)).checkStatus(anyString());
        verify(fileStatusProcessor, times(1)).saveFile(any(FileDto.class));
        verify(kafkaTemplate, times(1)).send(eq("upload"), anyString());
        verify(kafkaTemplate, times(1)).send(eq("status"), anyString());

        assertEquals(HttpStatus.OK, response.getHttpStatus());
        assertThat(response.getHashFile()).isNotNull();
    }

    @Test
    void upload_JsonProcessingException() throws Exception {
        when(fileStatusProcessor.checkStatus(anyString())).thenReturn(null);
        when(objectMapper.writeValueAsString(any(FileDto.class))).thenThrow(JsonProcessingException.class);

        assertThrows(JsonProcessingException.class, () -> fileUploadService.upload(testFile));

        verify(fileStatusProcessor, times(1)).checkStatus(anyString());
        verify(fileStatusProcessor, times(1)).saveFile(any(FileDto.class));
        verify(objectMapper, times(1)).writeValueAsString(any(FileDto.class));
        verifyNoMoreInteractions(kafkaTemplate);
    }


}
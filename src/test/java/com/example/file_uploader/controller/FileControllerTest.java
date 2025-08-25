package com.example.file_uploader.controller;

import com.example.file_uploader.dto.ResponseDto;
import com.example.file_uploader.exception.FileAlreadyUploadedException;
import com.example.file_uploader.exception.UnsupportedFileException;
import com.example.file_uploader.service.FileUploadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FileController.class)
@AutoConfigureMockMvc
class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FileUploadService fileUploadService;

    private MockMultipartFile file;
    private MockMultipartFile emptyFile;

    @BeforeEach
    void setUp() {
        file = new MockMultipartFile(
                "file",
                "test.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "excel-content".getBytes()
        );

        emptyFile = new MockMultipartFile(
                "file",
                "test.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                new byte[0]
        );
    }

    @Test
    void upload_ValidFile_ShouldReturnResponseDto() throws Exception {

        ResponseDto expectedResponse = new ResponseDto(HttpStatus.OK, "checksum123456789");

        when(fileUploadService.upload(any())).thenReturn(expectedResponse);

        mockMvc.perform(multipart("/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.httpStatus").value("OK"))
                .andExpect(jsonPath("$.hashFile").value("checksum123456789"));
    }

    @Test
    void upload_UnsupportedFileException() throws Exception {

        when(fileUploadService.upload(any()))
                .thenThrow(new UnsupportedFileException("The file is empty", HttpStatus.INTERNAL_SERVER_ERROR));

        mockMvc.perform(multipart("/upload").file(emptyFile))
                .andExpect(status().is(500))
                .andExpect(jsonPath("$.httpStatus").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.message").value("The file is empty"));
    }

    @Test
    void upload_FileAlreadyUploadedException() throws Exception {

        when(fileUploadService.upload(any()))
                .thenThrow(new FileAlreadyUploadedException(
                        "This file has already been uploaded.",
                        HttpStatus.INTERNAL_SERVER_ERROR));

        mockMvc.perform(multipart("/upload").file(emptyFile))
                .andExpect(status().is(500))
                .andExpect(jsonPath("$.httpStatus").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.message").value("This file has already been uploaded."));
    }

    @Test
    void upload_MaxUploadSizeExceededException() throws Exception {

        when(fileUploadService.upload(any()))
                .thenThrow(new MaxUploadSizeExceededException(5000));

        mockMvc.perform(multipart("/upload").file(emptyFile))
                .andExpect(status().is(500))
                .andExpect(jsonPath("$.httpStatus").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.message").value("Maximum upload size of 5000 bytes exceeded"));
    }

}
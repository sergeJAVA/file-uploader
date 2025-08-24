package com.example.file_uploader.dto;

import com.example.file_uploader.constant.FileStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileDto {

    private String fileName;

    private byte[] fileBytes;

    private FileStatus fileStatus;

    private String checksum;

}

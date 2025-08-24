package com.example.file_uploader.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CheckStatusRequest {

    private String checksum;

}

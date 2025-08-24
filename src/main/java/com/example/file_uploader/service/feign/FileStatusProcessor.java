package com.example.file_uploader.service.feign;

import com.example.file_uploader.dto.CheckStatusRequest;
import com.example.file_uploader.dto.FileDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Component
@FeignClient(name = "file-status-processor", url = "${feign.file-status-processor.url}")
public interface FileStatusProcessor {

    @GetMapping
    FileDto checkStatus(@RequestParam String checksum);

    @PostMapping
    void saveFile(@RequestBody FileDto fileDto);

}

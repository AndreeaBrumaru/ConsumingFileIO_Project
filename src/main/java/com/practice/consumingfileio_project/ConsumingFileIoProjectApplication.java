package com.practice.consumingfileio_project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@SpringBootApplication
@RestController
public class ConsumingFileIoProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConsumingFileIoProjectApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder)
    {
        return  builder.build();
    }

    @GetMapping("/download/{fileName}")
    public File downloadFile(@PathVariable String fileName) throws IOException
    {
        RestTemplate restTemplate = new RestTemplate();
        String resourceUrl = "http://localhost:8080/downloadFile/" + fileName;
        byte[] fileBytes = restTemplate.getForObject(resourceUrl, byte[].class);
        return Files.write(Paths.get(fileName), fileBytes).toFile();
    }

    @PostMapping("/upload")
    public void uploadFile(@RequestParam("file")MultipartFile file) throws IOException {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, String> fileMap = new LinkedMultiValueMap<>();
        ContentDisposition contentDisposition = ContentDisposition
                .builder("form-data")
                .name(file.getName())
                .filename(file.getOriginalFilename())
                .build();

        fileMap.add(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());
        HttpEntity<byte[]> fileEntity = new HttpEntity<>(file.getBytes(), fileMap);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", fileEntity);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try
        {
            ResponseEntity<String> response = restTemplate.exchange(
                    "http://localhost:8080/uploadFile",
                    HttpMethod.POST,
                    requestEntity,
                    String.class);
        }
        catch (HttpClientErrorException ex)
        {
            ex.printStackTrace();
        }
    }
}

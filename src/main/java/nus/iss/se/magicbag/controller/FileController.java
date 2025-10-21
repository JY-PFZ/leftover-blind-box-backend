package nus.iss.se.magicbag.controller;

import lombok.RequiredArgsConstructor;
import nus.iss.se.magicbag.service.S3StorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class FileController {

    private final S3StorageService s3StorageService;

    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file) throws IOException {
        String key = "uploads/" + file.getOriginalFilename();
        Path tempFile = Files.write(Files.createTempFile("upload-", ""), file.getBytes());
        s3StorageService.upload(key, tempFile);
        return "Uploaded to S3: " + key;
    }

    @GetMapping("/download/{key}")
    public ResponseEntity<byte[]> download(@PathVariable String key) throws IOException {
        key = "uploads/" + key;
        if (!s3StorageService.exists(key)) {
            throw new RuntimeException("File not found");
        }
        byte[] data = s3StorageService.downloadAsBytes(key);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + key + "\"")
                .body(data);
    }

    @DeleteMapping("/delete/{key}")
    public String delete(@PathVariable String key) {
        s3StorageService.delete(key);
        return "Deleted: " + key;
    }
}

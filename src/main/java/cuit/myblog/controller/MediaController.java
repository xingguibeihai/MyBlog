package cuit.myblog.controller;

import cuit.myblog.payload.FileResponse;
import cuit.myblog.service.FileService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/media")
public class MediaController {

    private final FileService fileService;

    public MediaController(FileService fileService) {
        this.fileService = fileService;
    }

    // 允许所有已认证用户上传文件
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @PostMapping("/upload")
    public ResponseEntity<FileResponse> uploadFile(@RequestParam("file") MultipartFile file) {
        // 调用 Service 存储文件
        FileResponse fileResponse = fileService.storeFile(file);

        return ResponseEntity.ok(fileResponse);
    }

    // 如果需要批量上传，可以添加另一个方法
    // public List<FileResponse> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files)
}
package cuit.myblog.service.impl;

import cuit.myblog.exception.FileStorageException;
import cuit.myblog.payload.FileResponse;
import cuit.myblog.service.FileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileServiceImpl implements FileService {

    private final Path fileStorageLocation;

    // 1. 构造函数：获取配置中的文件存储路径
    public FileServiceImpl(@Value("${file.upload-dir}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();

        try {
            // 如果目录不存在则创建
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new FileStorageException("无法创建用于文件上传的目录: " + uploadDir, ex);
        }
    }

    // 2. 存储文件核心逻辑
    @Override
    public FileResponse storeFile(MultipartFile file) {
        // 规范化文件名
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());

        // 生成唯一文件名，防止覆盖 (这里使用时间戳+原始文件名作为示例)
        String fileName = System.currentTimeMillis() + "_" + originalFileName;

        try {
            // 检查文件名是否包含无效字符
            if (fileName.contains("..")) {
                throw new FileStorageException("文件名包含无效路径序列: " + fileName);
            }

            // 复制文件到目标位置 (覆盖同名文件)
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // 构建可供下载/访问的 URL
            String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/media/") // 对应 application.properties 中的 static-path-pattern
                    .path(fileName)
                    .toUriString();

            FileResponse response = new FileResponse();
            response.setFileName(fileName);
            response.setFileDownloadUri(fileDownloadUri);
            response.setFileType(file.getContentType());
            response.setSize(file.getSize());

            return response;

        } catch (IOException ex) {
            throw new FileStorageException("无法存储文件 " + fileName + ". 请重试!", ex);
        }
    }
}
package cuit.myblog.service;

import org.springframework.web.multipart.MultipartFile;
import cuit.myblog.payload.FileResponse;

public interface FileService {

    /**
     * 存储文件并返回文件信息
     * @param file 接收到的文件
     * @return FileResponse 包含文件名的信息
     */
    FileResponse storeFile(MultipartFile file);
}
package cuit.myblog.payload;

import lombok.Data;

@Data
public class FileResponse {
    private String fileName;
    private String fileDownloadUri; // 文件访问的完整 URL
    private String fileType;
    private long size;
}
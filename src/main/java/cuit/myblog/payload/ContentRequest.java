package cuit.myblog.payload;

import lombok.Data;

@Data
public class ContentRequest {
    // 💡 客户端在 POST/PUT 时只需要提供标题、内容和作者（注意：作者可能来自JWT，此处保留字段以便于理解）
    private String title;
    private String body;
    private String author;
}
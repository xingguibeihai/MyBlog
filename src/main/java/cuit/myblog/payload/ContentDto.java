package cuit.myblog.payload;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ContentDto {
    private Long id;
    private String title;
    private String body;
    private String author;
    private LocalDateTime publishedAt;
    private int commentCount;
    // 💡 注意：这里只包含需要暴露给客户端的字段
}
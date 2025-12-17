package cuit.myblog.payload;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CommentDto {
    private Long id;
    private String body;
    private String author;
    private LocalDateTime publishedAt;
}
package cuit.myblog.payload;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NewCommentDto {
    @NotEmpty(message = "评论内容不能为空")
    @Size(min = 2, message = "评论内容至少需要2个字符")
    private String body;
}
package cuit.myblog.payload;

import cuit.myblog.entity.Content;
import lombok.Data;
import java.util.List;

@Data
public class ContentResponse {

    // 当前页的文章列表
    private List<ContentDto> content;

    // 当前页码 (从 0 或 1 开始，取决于实现)
    private int pageNo;

    // 每页记录数
    private int pageSize;

    // 总记录数
    private long totalElements;

    // 总页数
    private int totalPages;

    // 是否为最后一页
    private boolean last;
}
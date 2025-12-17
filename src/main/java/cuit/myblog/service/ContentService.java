package cuit.myblog.service;

import cuit.myblog.payload.ContentRequest;
import cuit.myblog.payload.ContentDto;
import cuit.myblog.payload.ContentResponse;

public interface ContentService {

    // 1. 创建内容：接收 Request，返回 DTO
    ContentDto createContent(ContentRequest contentRequest);

    // 2. 获取所有内容：参数不变，返回 ContentResponse (内部包含 DTO 列表)
    ContentResponse getAllContents(int pageNo, int pageSize, String sortBy, String sortDir, String keyword);

    // 3. 根据 ID 获取：返回 DTO
    ContentDto getContentById(Long id);

    // 4. 更新内容：接收 Request，返回 DTO
    ContentDto updateContent(Long id, ContentRequest contentRequest);

    // 5. 删除内容：无返回值
    void deleteContent(Long id);

    // 6. 那个按作者查询的方法如果暂时没用到，可以先注释掉，或者也改为返回 ContentResponse
    // ContentResponse getContentsByAuthor(String author, int pageNo, int pageSize);
}
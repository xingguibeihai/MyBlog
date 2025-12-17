package cuit.myblog.controller;

import cuit.myblog.payload.ContentDto;
import cuit.myblog.payload.ContentRequest;
import cuit.myblog.payload.ContentResponse;
import cuit.myblog.service.ContentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/content")
public class ContentController {

    private final ContentService contentService;

    public ContentController(ContentService contentService) {
        this.contentService = contentService;
    }

    // 1. 创建新内容
    // 💡 关键修改：允许所有已认证用户 (ROLE_ADMIN 或 ROLE_USER) 发布内容。
    //    因为只有登录用户才能获取到 Token，进而获取到 username 来设置作者。
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @PostMapping
    public ResponseEntity<ContentDto> createContent(@RequestBody ContentRequest contentRequest) {
        ContentDto newContent = contentService.createContent(contentRequest);
        return new ResponseEntity<>(newContent, HttpStatus.CREATED);
    }

    // 2. 获取所有内容列表 (分页/排序/搜索) - 无权限要求
    @GetMapping
    public ResponseEntity<ContentResponse> getAllContents(
            @RequestParam(value = "pageNo", defaultValue = "0", required = false) int pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize,
            @RequestParam(value = "sortBy", defaultValue = "publishedAt", required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc", required = false) String sortDir,
            @RequestParam(value = "keyword", required = false) String keyword
    ) {
        return ResponseEntity.ok(contentService.getAllContents(pageNo, pageSize, sortBy, sortDir, keyword));
    }

    // 3. 根据 ID 获取单个内容 - 无权限要求
    @GetMapping("/{id}")
    public ResponseEntity<ContentDto> getContentById(@PathVariable Long id) {
        return ResponseEntity.ok(contentService.getContentById(id));
    }

    // 4. 更新内容
    // 权限保持不变：管理员 或 文章作者本人 (@contentSecurity.isContentOwnerOrAdmin(#id))
    @PreAuthorize("hasRole('ADMIN') or @contentSecurity.isContentOwnerOrAdmin(#id)")
    @PutMapping("/{id}")
    public ResponseEntity<ContentDto> updateContent(@PathVariable Long id, @RequestBody ContentRequest contentRequest) {
        ContentDto updatedContent = contentService.updateContent(id, contentRequest);
        return ResponseEntity.ok(updatedContent);
    }

// ContentController.java (修复后的版本)

    // 5. 删除内容
    // 权限保持不变：管理员 或 文章作者本人 (@contentSecurity.isContentOwnerOrAdmin(#id))
    @PreAuthorize("hasRole('ADMIN') or @contentSecurity.isContentOwnerOrAdmin(#id)")
    @DeleteMapping("/{id}")
    // 💥 关键修改：返回 ResponseEntity<Void> 和 HttpStatus.NO_CONTENT (204)
    public ResponseEntity<Void> deleteContent(@PathVariable Long id) {
        contentService.deleteContent(id);

        // 返回 204 No Content 响应，表示操作成功且无内容返回
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);

        // 原始代码：
        // return ResponseEntity.ok("Content deleted successfully with ID: " + id);
    }

// ----------------------------------------------------------------------------------------------------------------
// 或者，如果您必须返回消息，但希望确保前端逻辑不误判：
// ----------------------------------------------------------------------------------------------------------------
/*
    @PreAuthorize("hasRole('ADMIN') or @contentSecurity.isContentOwnerOrAdmin(#id)")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteContent(@PathVariable Long id) {
        contentService.deleteContent(id);
        // 返回 200 OK (但 204 是更标准的 RESTful 做法)
        return ResponseEntity.ok("Content deletion successful. Content ID: " + id);
    }
*/
}
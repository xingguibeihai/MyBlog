package cuit.myblog.service.impl;

import cuit.myblog.entity.Content;
import cuit.myblog.exception.ResourceNotFoundException;
import cuit.myblog.exception.BlogAPIException;
import cuit.myblog.payload.ContentDto;
import cuit.myblog.payload.ContentRequest;
import cuit.myblog.payload.ContentResponse;
import cuit.myblog.repository.CommentRepository; // 🌟 导入评论仓库 🌟
import cuit.myblog.repository.ContentRepository;
import cuit.myblog.service.ContentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ContentServiceImpl implements ContentService {

    private final ContentRepository contentRepository;
    private final CommentRepository commentRepository; // 🌟 声明评论仓库 🌟

    // 🌟 构造器注入修改：同时注入 ContentRepository 和 CommentRepository 🌟
    public ContentServiceImpl(ContentRepository contentRepository, CommentRepository commentRepository) {
        this.contentRepository = contentRepository;
        this.commentRepository = commentRepository;
    }

    // --- 辅助方法：Entity 转换为 DTO ---
    // 🌟 修改：新增一个接收 commentCount 参数的方法，便于统一处理 🌟
    private ContentDto mapToDto(Content content, int commentCount) {
        ContentDto contentDto = new ContentDto();
        contentDto.setId(content.getId());
        contentDto.setTitle(content.getTitle());
        contentDto.setBody(content.getBody());
        contentDto.setAuthor(content.getAuthor());
        contentDto.setPublishedAt(content.getPublishedAt());
        contentDto.setCommentCount(commentCount); // 🌟 关键：设置评论数量 🌟
        return contentDto;
    }

    // 🌟 重载 mapToDto，用于创建内容时，初始评论数为 0 🌟
    private ContentDto mapToDto(Content content) {
        // 创建时，评论数量默认为 0
        return mapToDto(content, 0);
    }

// ContentServiceImpl.java (修改后的 mapToEntity)

    // --- 辅助方法：Request DTO 转换为 Entity ---
    private Content mapToEntity(ContentRequest contentRequest, String authorName) {
        Content content = new Content();
        content.setTitle(contentRequest.getTitle());
        content.setBody(contentRequest.getBody());
        content.setAuthor(authorName);
        content.setPublishedAt(LocalDateTime.now());
        return content;
    }

    // --- 1. 创建新内容 (不变) ---
    @Override
    public ContentDto createContent(ContentRequest contentRequest) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        Content content = mapToEntity(contentRequest, currentUsername);
        Content newContent = contentRepository.save(content);
        // 使用重载方法，初始评论数为 0
        return mapToDto(newContent);
    }

    // --- 2. 获取所有内容 (分页/排序/搜索) ---
    @Override
    public ContentResponse getAllContents(int pageNo, int pageSize, String sortBy, String sortDir, String keyword) {

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ?
                Sort.by(sortBy).ascending() :
                Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

        Page<Content> contents;
        if (keyword != null && !keyword.trim().isEmpty()) {
            contents = contentRepository.findByTitleContainingOrBodyContaining(keyword, keyword, pageable);
        } else {
            contents = contentRepository.findAll(pageable);
        }

        // 🌟 修复点 1：遍历查询评论计数 🌟
        List<ContentDto> contentDtos = contents.getContent()
                .stream()
                .map(content -> {
                    // 1. 查询评论数量
                    int commentCount = commentRepository.countByContentId(content.getId());
                    // 2. 转换为 DTO 并设置评论数量
                    return mapToDto(content, commentCount);
                })
                .collect(Collectors.toList());

        // 5. 构建响应对象
        ContentResponse contentResponse = new ContentResponse();
        contentResponse.setContent(contentDtos);
        contentResponse.setPageNo(contents.getNumber());
        contentResponse.setPageSize(contents.getSize());
        contentResponse.setTotalElements(contents.getTotalElements());
        contentResponse.setTotalPages(contents.getTotalPages());
        contentResponse.setLast(contents.isLast());

        return contentResponse;
    }

    // --- 3. 根据 ID 获取单个内容详情 ---
    @Override
    public ContentDto getContentById(Long id) {
        Content content = contentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Content", "id", id));

        // 🌟 修复点 2：查询评论计数 🌟
        int commentCount = commentRepository.countByContentId(id);

        return mapToDto(content, commentCount);
    }

    // --- 4. 更新内容 (不变) ---
    @Override
    public ContentDto updateContent(Long id, ContentRequest contentRequest) {
        // ... (省略权限校验，假设它在Controller或拦截器中处理) ...
        Content content = contentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Content", "id", id));

        // 更新字段
        content.setTitle(contentRequest.getTitle());
        content.setBody(contentRequest.getBody());
        Content updatedContent = contentRepository.save(content);

        // 🌟 修复点 3：更新后需要重新获取评论计数 🌟
        int commentCount = commentRepository.countByContentId(id);

        return mapToDto(updatedContent, commentCount);
    }

    // --- 5. 删除内容 (不变) ---
    @Override
    @Transactional
    public void deleteContent(Long id) {
        // ... (删除逻辑与权限校验不变) ...
        Content content = contentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Content", "id", id));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        boolean isAuthor = content.getAuthor().equals(currentUsername);
        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_ADMIN"));

        if (!isAuthor && !isAdmin) {
            throw new BlogAPIException(HttpStatus.FORBIDDEN, "您无权删除这篇文章！只有作者或管理员才能删除。");
        }

        contentRepository.delete(content);
    }
}
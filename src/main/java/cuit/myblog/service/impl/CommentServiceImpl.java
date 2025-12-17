package cuit.myblog.service.impl;

import cuit.myblog.entity.Comment;
import cuit.myblog.entity.Content;
import cuit.myblog.entity.User;
import cuit.myblog.exception.BlogAPIException;
import cuit.myblog.exception.ResourceNotFoundException;
import cuit.myblog.payload.CommentDto;
import cuit.myblog.payload.NewCommentDto;
import cuit.myblog.repository.CommentRepository;
import cuit.myblog.repository.ContentRepository;
import cuit.myblog.repository.UserRepository;
import cuit.myblog.service.CommentService;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
// 🌟 新增导入：用于角色检查 🌟
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final ContentRepository contentRepository;
    private final UserRepository userRepository;
    private final ModelMapper mapper;

    public CommentServiceImpl(CommentRepository commentRepository,
                              ContentRepository contentRepository,
                              UserRepository userRepository,
                              ModelMapper mapper) {
        this.commentRepository = commentRepository;
        this.contentRepository = contentRepository;
        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    // --- 辅助方法：Entity 转换为 DTO ---
    private CommentDto mapToDto(Comment comment) {
        return mapper.map(comment, CommentDto.class);
    }

    // --- 辅助方法：获取当前认证对象 ---
    private Authentication getCurrentAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    // --- 辅助方法：获取当前登录用户名 ---
    private String getCurrentUsername() {
        return getCurrentAuthentication().getName();
    }

    // --- 1. 创建评论 (保持不变) ---
    @Override
    @Transactional
    public CommentDto createComment(Long contentId, NewCommentDto commentDto) {
        String currentUsername = getCurrentUsername();

        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", currentUsername));

        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new ResourceNotFoundException("Content", "id", contentId));

        Comment comment = Comment.builder()
                .body(commentDto.getBody())
                .author(user.getUsername())
                .userId(user.getId())
                .content(content)
                .build();

        Comment savedComment = commentRepository.save(comment);
        return mapToDto(savedComment);
    }

    // --- 2. 获取评论列表 (保持不变) ---
    @Override
    public List<CommentDto> getCommentsByContentId(Long contentId) {
        if (!contentRepository.existsById(contentId)) {
            throw new ResourceNotFoundException("Content", "id", contentId);
        }

        List<Comment> comments = commentRepository.findByContentIdOrderByPublishedAtDesc(contentId);
        return comments.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // --- 3. 更新评论 (已修复权限校验) ---
    @Override
    @Transactional
    public CommentDto updateComment(Long contentId, Long commentId, CommentDto commentRequest) {

        // 1. 验证文章和评论是否存在
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new ResourceNotFoundException("Content", "id", contentId));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));

        // 2. 验证评论是否属于该文章 (数据完整性检查)
        if (!comment.getContent().getId().equals(content.getId())) {
            throw new BlogAPIException(HttpStatus.BAD_REQUEST, "评论不属于该文章！");
        }

        // 3. 权限校验：允许作者或管理员修改
        Authentication authentication = getCurrentAuthentication();
        String currentUsername = authentication.getName();

        boolean isAuthor = comment.getAuthor().equals(currentUsername);

        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_ADMIN"));

        if (!isAuthor && !isAdmin) {
            throw new BlogAPIException(HttpStatus.FORBIDDEN, "您无权编辑该评论！只有评论作者或管理员才能修改。");
        }
        // -----------------------------------------------------

        // 4. 执行更新 (只允许更新 Body 字段)
        comment.setBody(commentRequest.getBody());

        // 5. 保存并返回 DTO
        Comment updatedComment = commentRepository.save(comment);
        return mapToDto(updatedComment);
    }

    // --- 4. 🌟 新增方法：删除评论 (带权限校验) 🌟 ---
    @Override
    @Transactional
    public void deleteComment(Long contentId, Long commentId) {

        // 1. 验证文章和评论是否存在
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new ResourceNotFoundException("Content", "id", contentId));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));

        // 2. 验证评论是否属于该文章 (数据完整性检查)
        if (!comment.getContent().getId().equals(content.getId())) {
            throw new BlogAPIException(HttpStatus.BAD_REQUEST, "评论不属于该文章！");
        }

        // 3. 🌟 权限校验逻辑 🌟
        Authentication authentication = getCurrentAuthentication();
        String currentUsername = authentication.getName();

        // 检查是否为评论作者
        boolean isAuthor = comment.getAuthor().equals(currentUsername);

        // 检查是否为管理员 (检查是否存在 ROLE_ADMIN 权限)
        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_ADMIN"));

        // 如果既不是作者，也不是管理员，则拒绝访问
        if (!isAuthor && !isAdmin) {
            // 抛出 403 Forbidden
            throw new BlogAPIException(HttpStatus.FORBIDDEN, "您无权删除该评论！只有评论作者或管理员才能删除。");
        }
        // -----------------------------------------------------

        // 4. 执行删除操作
        commentRepository.delete(comment);
    }
}
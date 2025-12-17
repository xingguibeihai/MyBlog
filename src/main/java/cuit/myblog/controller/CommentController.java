package cuit.myblog.controller;

import cuit.myblog.payload.CommentDto;
import cuit.myblog.payload.NewCommentDto;
import cuit.myblog.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/content/{contentId}/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    // 发布评论：需要登录
    @PostMapping
    public ResponseEntity<CommentDto> createComment(
            @PathVariable(value = "contentId") Long contentId,
            @Valid @RequestBody NewCommentDto commentDto) {
        return new ResponseEntity<>(commentService.createComment(contentId, commentDto), HttpStatus.CREATED);
    }

    // 获取评论列表：公开
    @GetMapping
    public List<CommentDto> getCommentsByContentId(
            @PathVariable(value = "contentId") Long contentId) {
        return commentService.getCommentsByContentId(contentId);
    }

    // 编辑/更新评论：需要登录，需权限校验 (作者或管理员)
    // 路径: PUT /api/content/{contentId}/comments/{commentId}
    @PutMapping("/{commentId}")
    public ResponseEntity<CommentDto> updateComment(
            @PathVariable(value = "contentId") Long contentId,
            @PathVariable(value = "commentId") Long commentId,
            @Valid @RequestBody CommentDto commentDto) {

        CommentDto updatedComment = commentService.updateComment(contentId, commentId, commentDto);

        return new ResponseEntity<>(updatedComment, HttpStatus.OK);
    }

    // 🌟 新增方法：删除评论 🌟
    // 路径: DELETE /api/content/{contentId}/comments/{commentId}
    // 需要登录，需权限校验 (作者或管理员)
    @DeleteMapping("/{commentId}")
    public ResponseEntity<String> deleteComment(
            @PathVariable(value = "contentId") Long contentId,
            @PathVariable(value = "commentId") Long commentId) {

        // 调用 Service 层方法，其中包含权限校验
        commentService.deleteComment(contentId, commentId);

        // 删除成功，返回 204 No Content
        return new ResponseEntity<>("评论删除成功！", HttpStatus.NO_CONTENT);
    }
}
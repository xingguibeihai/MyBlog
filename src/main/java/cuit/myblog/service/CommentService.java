package cuit.myblog.service;

import cuit.myblog.payload.CommentDto;
import cuit.myblog.payload.NewCommentDto;
import java.util.List;

public interface CommentService {

    // 创建评论
    CommentDto createComment(Long contentId, NewCommentDto commentDto);

    // 获取评论列表
    List<CommentDto> getCommentsByContentId(Long contentId);

    // 更新评论 (已实现管理员权限校验)
    CommentDto updateComment(Long contentId, Long commentId, CommentDto commentDto);

    // 🌟 新增方法声明：删除评论 (将由 CommentServiceImpl 实现管理员权限校验) 🌟
    void deleteComment(Long contentId, Long commentId);
}
package com.example.service;

import com.example.model.domain.Comment;
import com.github.pagehelper.PageInfo;

public interface ICommentService {
    //获取文章下的评论  查询所有评论需要文章id 以及page count
    public PageInfo<Comment> getComments(Integer aid,int page,int count);

    //用户发表评论
    public void pushComment(Comment comment);
}

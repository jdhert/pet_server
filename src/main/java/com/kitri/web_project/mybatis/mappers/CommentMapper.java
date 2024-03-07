package com.kitri.web_project.mybatis.mappers;

import com.kitri.web_project.dto.comment.CommentDto;
import com.kitri.web_project.dto.comment.RequestComment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentMapper {
    List<CommentDto> getComments(long id);

    List<CommentDto> getMyComments(long id);

    void addComment(RequestComment requestComment);


}

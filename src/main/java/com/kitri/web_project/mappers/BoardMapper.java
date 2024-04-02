package com.kitri.web_project.mappers;

import com.kitri.web_project.dto.board.BoardInfo;
import com.kitri.web_project.dto.board.RequestBoard;
import com.kitri.web_project.dto.board.UpdateBoard;
import com.kitri.web_project.dto.image.RequestImage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BoardMapper {
    List<BoardInfo> getBoards(int offset ,int limit, int subject);
    List<BoardInfo> getPopularBoards(String interval, int subject);
    void uploadBoard(RequestBoard board);
    List<BoardInfo> getSearchBoards(String search, String type, String type1, int offset, int limit, int subject);
    void updateViewCount(long id);

    BoardInfo getBoard(long id);
    void setTag(long id, String tag);
    void setImage(long id, String img);

    List<String> getTags(long id);


    void updateBoard(UpdateBoard updateBoard);

    void deleteBoard(long id);
    void deleteAllImgs(long id);

    void deleteTags(long id);

    List<BoardInfo> getMyBoards(long id, int subject, int offset, int itemsPerPage);

    void incrementLikeCount(long postId);
    void decrementLikeCount(long postId);

    void incrementCommentLikeCount(long commentId);
    void decrementCommentLikeCount(long commentId);

    void setImage(long id, long boardId, String imagePath);

    List<String> getImages(long id);

    List<BoardInfo> getMyLike(long id, int page);


    boolean checkLikeExists(@Param("userId") long userId, @Param("boardId") long boardId);

    boolean insertLike(long userId, long boardId);
    boolean deleteLike(long userId, long boardId);

    boolean getPostLikeStatus(Long postId);

    Long totalViewCount(long id);

    List<RequestImage> getBoardImage(long id);
    void deleteImageById(@Param("id") long id);



}

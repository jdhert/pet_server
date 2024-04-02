package com.kitri.web_project.service;

import com.kitri.web_project.dto.board.BoardInfo;
import com.kitri.web_project.mappers.BoardMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class BoardService {

    @Autowired
    BoardMapper boardMapper;

    private static final String frontendUrl = System.getenv("FRONTEND_URL");

    public List<BoardInfo> boardInfos(long id, int page){
        return boardMapper.getMyLike(id, (page-1) * 10);
    }

    public List<BoardInfo> popularBoards(int subject){
        String[] intervals = {"7 DAY", "1 MONTH", "1 YEAR", "100 YEAR"};
        List<BoardInfo> results = null;
        for (String interval : intervals) {
            results = boardMapper.getPopularBoards(interval, subject);
            if (!results.isEmpty()) {
                break;
            }
        }
        ImageSet(results);
        return results;
    }

    public void ImageSet(List<BoardInfo> bm){
        for(BoardInfo b : bm){
            ResponseEntity<List<String>> s = getImages(b.getId());
            if(!Objects.requireNonNull(s.getBody()).isEmpty())
                b.setImgPath(Objects.requireNonNull(s.getBody()).get(0));
        }
    }

    public ResponseEntity<List<String>> getImages(long boardId) {
        List<String> images = boardMapper.getImages(boardId);
        List<String> imageUrls = images.stream()
                .map(path -> frontendUrl + "/images/" + path)
                .collect(Collectors.toList());
//                .map(path -> ServletUriComponentsBuilder.fromCurrentContextPath()
//                        .path("/images/")
//                        .path(path)
//                        .toUriString())
//                .map(encodedUrl -> URLDecoder.decode(encodedUrl, StandardCharsets.UTF_8)) // URL 디코딩
//                .collect(Collectors.toList());
        return ResponseEntity.ok(imageUrls);
    }

//    public List<BoardInfo> getMyBoards(long id, int subject, int page){
//        int maxPage=10;
//        int offset;
//        int limit = 10;
//        offset = (page - 1) * maxPage;
//        return boardMapper.getMyBoards(id, subject);
//    }
}

package com.kitri.web_project.controller;

import com.kitri.web_project.dto.board.BoardInfo;
import com.kitri.web_project.dto.board.RequestBoard;
import com.kitri.web_project.dto.board.RequestBoardLike;
import com.kitri.web_project.dto.board.UpdateBoard;
import com.kitri.web_project.dto.image.RequestImage;
import com.kitri.web_project.mappers.BoardMapper;
import com.kitri.web_project.service.BoardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/free")
public class BoardController {

    @Autowired
    BoardMapper boardMapper;


    private final BoardService boardService;

    @Autowired
    public BoardController(BoardService boardService){
        this.boardService = boardService;
    }

    @GetMapping("/{page}")
    public List<BoardInfo> get(@PathVariable int page, @RequestParam int subject) {
        int maxPage = 8;
        int offset;
        int limit;
        offset = (page - 1) * maxPage;
        limit = 8;
        List<BoardInfo> bm = boardMapper.getBoards(offset, limit, subject);
        ImageSet(bm);
        return bm;
    }

    @PostMapping
    public void uploadBoard(@RequestBody RequestBoard board) {
        boardMapper.uploadBoard(board);
        for(String tag : board.getTags())
            boardMapper.setTag(board.getId(), tag);
        if(board.getImages()!=null) {
            for (String image : board.getImages())
                boardMapper.setImage(board.getUserId(), board.getId(), image);
        }
    }

    @GetMapping("/search/{page}")
    public List<BoardInfo> search(@RequestParam String search, @RequestParam String type, @RequestParam String type1, @RequestParam int subject, @PathVariable int page){
        int maxPage=8;
        int offset;
        int limit = 8;
        offset = (page - 1) * maxPage;
        List<BoardInfo> bm = boardMapper.getSearchBoards(search+'%', type, type1, offset, limit, subject);
        ImageSet(bm);
        return bm;
    }

    @GetMapping("/get/{boardId}")
    public BoardInfo getBoard(@PathVariable long boardId){
        return boardMapper.getBoard(boardId);
    }

    @GetMapping("/getTag/{boardId}")
    public List<String>getTagS(@PathVariable long boardId){
        return boardMapper.getTags(boardId);
    }

    @GetMapping("/getImage/{boardId}")
    public ResponseEntity<List<String>> getImages(@PathVariable long boardId){
        List<String> images = boardMapper.getImages(boardId);
        List<String> imageUrls = images.stream()
                .map(path -> ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path("/images/")
                        .path(path)
                        .toUriString())
                .map(encodedUrl -> URLDecoder.decode(encodedUrl, StandardCharsets.UTF_8)) // URL 디코딩
                .collect(Collectors.toList());
        return ResponseEntity.ok(imageUrls);
    }


    @PutMapping
    public void updateBoard(@RequestBody UpdateBoard updateBoard){
        boardMapper.deleteTags(updateBoard.getBoardId());
        boardMapper.updateBoard(updateBoard);
        for(String tag : updateBoard.getTags())
            boardMapper.setTag(updateBoard.getBoardId(), tag);
        for(String img : updateBoard.getImg())
            boardMapper.setImage(updateBoard.getUserId(), updateBoard.getBoardId(), img);
    }


    @DeleteMapping("/{id}")
    public void deleteBoard(@PathVariable long id){
        boardMapper.deleteBoard(id);
        boardMapper.deleteAllImgs(id);
    }

    @GetMapping("/getMyBoard/{id}")
    public List<BoardInfo> getMyBoard(@RequestParam int subject, @RequestParam int page, @RequestParam int itemsPerPage, @PathVariable long id) {
        int offset = (page - 1) * itemsPerPage;
        return boardMapper.getMyBoards(id, subject, offset, itemsPerPage);
    }

    @GetMapping("/popular")
    public List<BoardInfo> getPopularBoard(int subject) {
        return boardService.popularBoards(subject);
    }


    @PostMapping(value = "/img", consumes={MediaType.MULTIPART_FORM_DATA_VALUE})
    public List<String> insertImages(@RequestPart(value = "image", required = false) MultipartFile[] imageFiles) {
        List<String> s = new ArrayList<>();
        String uploadRootPath = "/app/images";


        for (MultipartFile file : imageFiles) {
            try {
                String originalFilename = file.getOriginalFilename();
                String uploadFileName = UUID.randomUUID() + "_" + originalFilename;

//                String currentDir = System.getProperty("user.dir");
//                Path parentDir = Paths.get(currentDir).getParent();
//                String uploadRootPath  = parentDir.resolve("images").toString();
                File uploadDir = new File(uploadRootPath);
                if (!uploadDir.exists()) {
                    uploadDir.mkdirs();
                }

//                File uploadFile = new File(uploadRootPath + File.separator + uploadFileName);
//                file.transferTo(uploadFile);
//                String savePath = uploadRootPath.substring(uploadRootPath.length()).replace("\\", "/");
//                String encodedFileName = URLEncoder.encode(uploadFileName, StandardCharsets.UTF_8);
//                String s1 = savePath + "/" + encodedFileName;
//                s.add(s1);
                String filePath = uploadRootPath + "/" + uploadFileName;
                File dest = new File(filePath);
                file.transferTo(dest);

                s.add(uploadFileName);
            } catch(IOException e){
                System.out.println(e);
            }
        }
        return s;
    }

    //수정 페이지 이미지 불러오기
    @GetMapping("/BoardEditImages/{id}")
    public ResponseEntity<List<RequestImage>> getBoardImage(@PathVariable long id) {
        List<RequestImage> imageList = boardMapper.getBoardImage(id);
        List<String> images = new ArrayList<>();
        for(RequestImage bs : imageList){
            images.add(bs.getImagePath());
        }
        List<String> imageUrls = images.stream()
                .map(path -> ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path("/images/")
                        .path(path)
                        .toUriString())
                .map(encodedUrl -> URLDecoder.decode(encodedUrl, StandardCharsets.UTF_8)) // URL 디코딩
                .toList();
        for (int i = 0; i < imageUrls.size(); i++) {
            imageList.get(i).setImagePath(imageUrls.get(i));
        }
        return ResponseEntity.ok(imageList);
    }

    //수정 이미지 삭제
    @DeleteMapping("/delete")
    public void deleteImageById(@RequestParam("ids") Long[] ids) {
        for (Long id : ids) {
            boardMapper.deleteImageById(id);
        }
    }

    @PutMapping("/{postId}/like")
    public void updateLikeStatus(@PathVariable long postId, @RequestParam boolean liked) {
        if(liked) {
            boardMapper.incrementLikeCount(postId);
        } else {
            boardMapper.decrementLikeCount(postId);
        }
    }


    @PutMapping("/{commentId}/commentLike")
    public void updateCommentLikeStatus(@PathVariable long commentId, @RequestParam boolean liked) {
        if(liked) {
            boardMapper.incrementCommentLikeCount(commentId);
        } else {
            boardMapper.decrementCommentLikeCount(commentId);
        }
    }

    public void ImageSet(List<BoardInfo> bm){
        for(BoardInfo b : bm){
            ResponseEntity<List<String>> s = getImages(b.getId());
            if(!Objects.requireNonNull(s.getBody()).isEmpty())
                b.setImgPath(Objects.requireNonNull(s.getBody()).get(0));
        }
    }

    @PutMapping("/view/{id}")
    public ResponseEntity<?> updateViewCount(@PathVariable("id") long id) {
        try {
            boardMapper.updateViewCount(id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/view/{id}")
    public ResponseEntity<Long> totalViewCount(@PathVariable("id") long id) {
        Long viewCount = boardMapper.totalViewCount(id);
        return ResponseEntity.ok().body(viewCount);
    }

    @PostMapping("/liked")
    public boolean boardLiked(@RequestBody RequestBoardLike requestBoardLike) {
        try {
            if (requestBoardLike.getLiked()) {
                boolean recordExists = boardMapper.checkLikeExists(requestBoardLike.getUserId(), requestBoardLike.getBoardId());
                if (!recordExists) {
                    boardMapper.insertLike(requestBoardLike.getUserId(), requestBoardLike.getBoardId());
                }
                return true;
            } else {
                boardMapper.deleteLike(requestBoardLike.getUserId(), requestBoardLike.getBoardId());
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @GetMapping("/{id}/likeStatus")
    public boolean getPostLikeStatus(@PathVariable("id") Long postId) {
        try {
            return boardMapper.getPostLikeStatus(postId);
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }


    @GetMapping("/getMyLike/{id}")
    public List<BoardInfo> getMyLike(@PathVariable long id, int page){
        return boardService.boardInfos(id, page);
    }
}

package com.kitri.web_project.controller;

import com.kitri.web_project.dto.DiaryInfo;
import com.kitri.web_project.dto.PetInfo;
import com.kitri.web_project.dto.UserInfo;
import com.kitri.web_project.dto.UserUpdateInfo;
import com.kitri.web_project.dto.api.BookMarks;
import com.kitri.web_project.dto.diary.DiaryImgDto;
import com.kitri.web_project.dto.diary.DiaryMainImg;
import com.kitri.web_project.dto.diary.PetCalendar;
import com.kitri.web_project.dto.diary.RequestDiary;
import com.kitri.web_project.mappers.UserMapper;
import com.kitri.web_project.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.File;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@RestController
@RequestMapping("/api/myinfo")
public class UserInfoController {

    @Autowired
    UserMapper userMapper;

    private final UserInfoService userInfoService;

    @Autowired
    public UserInfoController(UserInfoService userInfoService){
        this.userInfoService = userInfoService;
    }

    @GetMapping("/{id}")
    public UserInfo getInfo(@PathVariable String id) {
        long id1 = Long.parseLong(id);
        return userMapper.findById(id1);
    }

    @PutMapping
    public void updateUser(@RequestBody UserUpdateInfo userUpdateInfo) {

        String imgPath = userMapper.getUserImages(userUpdateInfo.getUserId());
        userMapper.updateUser(userUpdateInfo);
//        String currentDir = System.getProperty("user.dir");
//        Path parentDir = Paths.get(currentDir).getParent();
//        String uploadRootPath  = parentDir.resolve("images").toString();
        String uploadRootPath = "/app/images";
        String fullPath = uploadRootPath + imgPath;
        File file = new File(fullPath);
        if (file.exists()) {
            try {
                if (file.delete()) {
                    System.out.println("Success: Image deleted");
                } else {
                    System.out.println("Failed: Image could not be deleted");
                }
            } catch (SecurityException e) {
                System.out.println("Failed: Security Exception occurred while deleting image");
                e.printStackTrace();
            }
        } else {
            System.out.println("Failed: Image not found at path " + fullPath);
        }
    }

    @PostMapping("/updatepw/{id}")
    public boolean updateNewPassword(@PathVariable Long id, @RequestBody Map<String, String> passwords) {
        String currentPassword = passwords.get("currentPassword");
        String newPassword = passwords.get("newPassword");
        String passwordFind = userMapper.passwordFind(id);

        if(!Objects.equals(passwordFind, currentPassword))
            return false;
        else {
            List<Object> params = Arrays.asList(id, currentPassword, newPassword);
            userMapper.updateNewPassword(params);
            return true;
        }
    }

    @DeleteMapping("/user/{id}")
    public void deleteUser(@PathVariable Long id) {
        String imgPath = userMapper.getUserImages(id);
        userMapper.deleteUser(id);

        String uploadRootPath = "/app/images";
        String fullPath = uploadRootPath + imgPath;
        File file = new File(fullPath);
        if (file.exists()) {
            try {
                if (file.delete()) {
                    System.out.println("Success: Image deleted");
                } else {
                    System.out.println("Failed: Image could not be deleted");
                }
            } catch (SecurityException e) {
                System.out.println("Failed: Security Exception occurred while deleting image");
                e.printStackTrace();
            }
        } else {
            System.out.println("Failed: Image not found at path " + fullPath);
        }
    }

    @GetMapping("/img/{id}")
    public ResponseEntity<String> getUserImages(@PathVariable long id){
        String images = userMapper.getUserImages(id);
        if(images==null)
            return null;

        if (images.startsWith("http")) {
            return ResponseEntity.ok(images);
        } else {
            String imageUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/images/")
                    .path(images)
                    .toUriString();
            imageUrl = URLDecoder.decode(imageUrl, StandardCharsets.UTF_8); // URL 디코딩
            return ResponseEntity.ok(imageUrl);
        }
    }

    @GetMapping("/pet/{id}")
    public List<PetInfo> getPet(@PathVariable String id) {
        long id1 = Long.parseLong(id);
        return userMapper.getPets(id1);
    }

    @GetMapping("/diary/{id}")
    public List<DiaryInfo> getDiary(@PathVariable String id) {
        long id1 = Long.parseLong(id);
        return userMapper.getDiary(id1);
    }
    //다이어리 등록 매핑
    @PostMapping
    public void adddiary(@RequestBody RequestDiary diaryInfo) {
        userMapper.save(diaryInfo);
        if(diaryInfo.getImg() != null) {
            for (String s : diaryInfo.getImg()) {
                userMapper.imageSave(s, diaryInfo.getUserId(), diaryInfo.getPetId(), diaryInfo.getId());
            }
        }
    }

    @GetMapping("/DiaryImages/{id}")
    public ResponseEntity<List<DiaryImgDto>> getImages(@PathVariable long id) {
        List<DiaryImgDto> imageList = userMapper.getDiaryImages(id);
        List<String> images = new ArrayList<>();

        for(DiaryImgDto ds : imageList){
            images.add(ds.getImgPath());
        }

        List<String> imageUrls = images.stream()
                .map(path -> ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path("/images/")
                        .path(path)
                        .toUriString())
                .map(encodedUrl -> URLDecoder.decode(encodedUrl, StandardCharsets.UTF_8)) // URL 디코딩
                .toList();
        for (int i = 0; i < imageUrls.size(); i++) {
            imageList.get(i).setImgPath(imageUrls.get(i));
        }

        ResponseEntity<List<DiaryImgDto>> l = ResponseEntity.ok(imageList);
        return ResponseEntity.ok(imageList);
    }

    @GetMapping("/select/{id}")
    public RequestDiary requestDiaries(@PathVariable long id){
        return userMapper.petDiary(id);
    }
    //다이어리 미리보기 매핑

    @DeleteMapping("/{diaryId}")
    public void RequestDiary(@PathVariable long diaryId) {
        userMapper.deleteDiary(diaryId);
        System.out.println("success");
    }

    @GetMapping("/calendar/{id}")
    public List<PetCalendar>petCalendars(@PathVariable long id){

        return userMapper.petCalendar(id);
    }
    @PostMapping("/updateColor/{petId}") //캘린더 색상 업데이트
    public void petCalendars(@PathVariable long petId, @RequestBody Map<String, String> requestBody){
        String color = requestBody.get("petColor");
        userMapper.UpdateColor(petId, color);
    }

    @GetMapping("/update/{id}") //다이어리 수정 불러오기
    public List<RequestDiary>requestDiaries1(@PathVariable long id){
        return userMapper.UpdateDiary(id);
    }

    @PutMapping ("/edit/{id}") //다이어리 수정
    public void editDiary(@RequestBody RequestDiary requestDiary){
        userMapper.editDiary(requestDiary);
        for(String s : requestDiary.getImg())
            userMapper.imageSave(s, requestDiary.getUserId(),requestDiary.getPetId(), requestDiary.getId());
    }

    @DeleteMapping("/delete")
    public void deleteImageById(@RequestParam("ids") Long[] ids) {
        for (Long id : ids) {
            userMapper.deleteImageById(id);
        }
    }

    @GetMapping("/getMainImage/{id}")
    public List<DiaryMainImg> diaryMainImages(@PathVariable long id, int page) {
        List<DiaryMainImg> diaryMainImgList = userMapper.diaryMainImages(id, (page-1)*12);

        if (diaryMainImgList != null && !diaryMainImgList.isEmpty()) {
            int maxPage = userMapper.getMaxPage(id); // userMapper를 통해 최대 페이지 값을 가져옴


            diaryMainImgList.get(0).setMaxPage(maxPage); // 각 DiaryMainImg 객체에 최대 페이지 값 설정


            // imgPath 데이터만 추출하여 디코딩된 URL 리스트 생성
            List<String> decodedImageUrls = diaryMainImgList.stream()
                    .map(diaryMainImg -> decodeImageUrl(diaryMainImg.getImgPath()))
                    .toList();

            // 디코딩된 URL을 다시 imgPath 필드에 할당하여 diaryMainImgList 수정
            for (int i = 0; i < diaryMainImgList.size(); i++) {
                diaryMainImgList.get(i).setImgPath(decodedImageUrls.get(i));
            }
        }

        return diaryMainImgList;
    }

    @GetMapping("/passVerify/{id}")
    public boolean passVerify(@PathVariable long id,String password){
        return userInfoService.passwordVerify(id, password);
    }

    private String decodeImageUrl(String encodedUrl) {
        return URLDecoder.decode(ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path("/images/")
                        .path(encodedUrl)
                        .toUriString(),
                StandardCharsets.UTF_8);
    }
}

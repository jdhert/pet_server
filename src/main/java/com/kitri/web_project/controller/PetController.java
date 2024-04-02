package com.kitri.web_project.controller;

import com.kitri.web_project.dto.PetInfo;
import com.kitri.web_project.dto.diary.PetCalendar;
import com.kitri.web_project.dto.pet.RequestPet;
import com.kitri.web_project.dto.pet.UpdatePet;
import com.kitri.web_project.dto.pet.getPetDiary;
import com.kitri.web_project.mappers.PetMapper;
import com.kitri.web_project.mappers.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.File;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/pet")
public class PetController {

    @Autowired
    private PetMapper petMapper;

    private static final String frontendUrl = System.getenv("FRONTEND_URL");

    @PostMapping
    public void addPet(@RequestBody RequestPet pet) {
        petMapper.addPet(pet);
    }

    @GetMapping("/{id}")
    public ResponseEntity<List<String>> getImages(@PathVariable long id){
        List<String> images = petMapper.getImages(id);
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

    @GetMapping("/detail/img/{petId}")
    public ResponseEntity<List<String>> getPetImages(@PathVariable long petId){
        List<String> images = petMapper.getPetImages(petId);
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

    @GetMapping("/detail/{petId}")
    public PetInfo getPet(@PathVariable Long petId) {
        return petMapper.getPet(petId);
    }

    @PutMapping
    public void updatePet(@RequestBody UpdatePet pet) {
        String imgPath = null;
        if (pet.getPetImg() == null) {
            petMapper.updatePet2(pet);
        } else {
            imgPath = String.valueOf(petMapper.getPetImages(pet.getPetId()));
            petMapper.updatePet(pet);
        }
        String uploadRootPath = "/app/images";
        String fullPath = uploadRootPath  + imgPath;
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

    @DeleteMapping("/{petId}")
    public void RequestDiary(@PathVariable long petId) {
        String imgPath = String.valueOf(petMapper.getPetImages(petId));
        petMapper.deletePet(petId);
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

    @GetMapping("/getDiary/{petId}")
    public List<getPetDiary> getDiary(@PathVariable long petId){
        List<getPetDiary> getPetDiaryList = petMapper.getDiary(petId);


        List<String> decodedImageUrls = getPetDiaryList.stream()
                .map(getdiaryImage -> decodeImageUrl(getdiaryImage.getImgPath()))
                .toList();

        for(int i = 0; i < getPetDiaryList.size(); i++){
            getPetDiaryList.get(i).setImgPath(decodedImageUrls.get(i));
        }
        return getPetDiaryList;
    }
    // URL 디코딩 메서드
    private String decodeImageUrl(String encodedUrl) {
//        return URLDecoder.decode(ServletUriComponentsBuilder.fromCurrentContextPath()
//                        .path("/images/")
//                        .path(encodedUrl)
//                        .toUriString(),
//                StandardCharsets.UTF_8);
            return frontendUrl + "/images/" + encodedUrl;

    }

}

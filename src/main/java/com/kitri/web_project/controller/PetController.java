package com.kitri.web_project.controller;

import com.kitri.web_project.dto.DiaryInfo;
import com.kitri.web_project.dto.PetInfo;
import com.kitri.web_project.dto.board.UpdateBoard;
import com.kitri.web_project.dto.pet.RequestPet;
import com.kitri.web_project.dto.pet.UpdatePet;
import com.kitri.web_project.mybatis.mappers.PetMapper;
import com.kitri.web_project.mybatis.mappers.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/pet")
public class PetController {

    @Autowired
    private PetMapper petMapper;

    @Autowired
    private UserMapper userMapper;

    @PostMapping
    public void addPet(@RequestBody RequestPet pet) {
        petMapper.addPet(pet);
    }

    @GetMapping("/{id}")
    public ResponseEntity<List<String>> getImages(@PathVariable long id){
        List<String> images = petMapper.getImages(id);
        // URI Components Builder를 사용해 완전한 URL 생성
        List<String> imageUrls = images.stream()
                .map(path -> ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path("/images/")
                        .path(path)
                        .toUriString())
                .map(encodedUrl -> URLDecoder.decode(encodedUrl, StandardCharsets.UTF_8)) // URL 디코딩
                .collect(Collectors.toList());
        return ResponseEntity.ok(imageUrls);
    }

    @GetMapping("/detail/img/{petId}")
    public ResponseEntity<List<String>> getPetImages(@PathVariable long petId){
        List<String> images = petMapper.getPetImages(petId);
        // URI Components Builder를 사용해 완전한 URL 생성
        List<String> imageUrls = images.stream()
                .map(path -> ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path("/images/")
                        .path(path)
                        .toUriString())
                .map(encodedUrl -> URLDecoder.decode(encodedUrl, StandardCharsets.UTF_8)) // URL 디코딩
                .collect(Collectors.toList());
        return ResponseEntity.ok(imageUrls);
    }

    @GetMapping("/detail/{petId}")
    public PetInfo getPet(@PathVariable Long petId) {
        return petMapper.getPet(petId);
    }

    @PutMapping
    public void updatePet(@RequestBody UpdatePet pet) {
        if (pet.getPetImg() == null) {
            petMapper.updatePet2(pet);
        } else {
            petMapper.updatePet(pet);
        }
    }

    @DeleteMapping("/{petId}")
    public void RequestDiary(@PathVariable long petId) {
        petMapper.deletePet(petId);
    }

}
package com.kitri.web_project.mybatis.mappers;

import com.kitri.web_project.dto.board.RequestBoard;
import com.kitri.web_project.dto.pet.RequestPet;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PetMapper {
    void addPet(RequestPet pet);


}

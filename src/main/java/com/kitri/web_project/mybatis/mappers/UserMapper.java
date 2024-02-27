package com.kitri.web_project.mybatis.mappers;

import com.kitri.web_project.signup.dto.ResponseUser;
import com.kitri.web_project.signup.dto.SignUpDto;
import org.apache.ibatis.annotations.Mapper;
import com.kitri.web_project.login.dto.ResponseUser;
import java.util.List;

@Mapper
public interface UserMapper {


    List<SignUpDto> memberInfo(); //회원조회
    ResponseUser findMember(String name, String email);
    void signup(String name, String email, String password, String address); //회원가입

    String findAll(); //전체조회
    ResponseUser findByEmail(String email);

}
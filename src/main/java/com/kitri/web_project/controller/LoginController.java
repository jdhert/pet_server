package com.kitri.web_project.controller;

import com.kitri.web_project.dto.login.LoginUser;
import com.kitri.web_project.dto.login.ResponseClient;
import com.kitri.web_project.dto.login.SocialLogin;
import com.kitri.web_project.mappers.UserMapper;
import com.kitri.web_project.service.LoginService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping("/api/login")
public class LoginController {

    @Autowired
    UserMapper userMapper;

    private final LoginService loginService;

    @Autowired
    public LoginController(LoginService loginService){
        this.loginService = loginService;
    }


    @PostMapping
    public ResponseEntity<Long> Log(@RequestBody LoginUser loginUser, HttpServletResponse response) {
        ResponseClient responseUsers = userMapper.findByEmail(loginUser.getEmail(), false);
//        if(responseUsers == null)
//            return 0L;
//        if(!Objects.equals(loginUser.getPassword(), responseUsers.getPassword()))
//            return 0L;
        if (responseUsers == null || !Objects.equals(loginUser.getPassword(), responseUsers.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(0L);
        }
        if(!responseUsers.getSocial()) {
            CookieSet(response, responseUsers, loginUser.getAutologin());
                // 만약 자동로그인 체크했을 경우 autologinCookie 생성
                if (loginUser.getAutologin() != null && loginUser.getAutologin()) {
                Cookie autologinCookie = new Cookie("autologin", "true");
                autologinCookie.setMaxAge(7 * 24 * 60 * 60); // 7 days in seconds
                autologinCookie.setPath("/");
                response.addCookie(autologinCookie);
            }
//            return responseUsers.getId();
            return ResponseEntity.ok(responseUsers.getId());
        }
//        return 0L;
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(0L);
    }

    @GetMapping("/logout")
    public boolean logOut(HttpServletResponse res){
        Cookie cookie = new Cookie("email", null);
        Cookie cookie1 = new Cookie("id", null);
        Cookie cookie2 = new Cookie("name", null);
        Cookie autologinCookie = new Cookie("autologin", "true");
        cookie1.setMaxAge(0);
        cookie.setMaxAge(0);
        cookie2.setMaxAge(0);
        autologinCookie.setMaxAge(0);
        cookie.setPath("/");
        cookie1.setPath("/");
        cookie2.setPath("/");
        autologinCookie.setPath("/");
        res.addCookie(cookie);
        res.addCookie(cookie1);
        res.addCookie(cookie2);
        res.addCookie(autologinCookie);
        return true;
    }

    @PostMapping("/social")
    public Long socialLogin(@RequestBody SocialLogin socialLogin, HttpServletResponse response){
       return loginService.socialLogin(socialLogin, response);
    }

    @GetMapping("/findPass")
    public boolean findPassword(String email){
        return loginService.findPassword(email);
    }

    @GetMapping("/sendCode")
    public void sendCode(String email){
        loginService.sendCode(email);
    }

    @GetMapping("/codeVerify")
    public boolean verifyCode(String code){
        return loginService.verifyCode(code);
    }

    public void CookieSet(HttpServletResponse response, ResponseClient responseClient, boolean check){
        Cookie cookie = new Cookie("email", responseClient.getEmail());
        Cookie cookie1 = new Cookie("id", responseClient.getId().toString());
        Cookie cookie2 = new Cookie("name", responseClient.getName());
        cookie.setPath("/");
        cookie1.setPath("/");
        cookie2.setPath("/");
        if(check) {
            cookie.setMaxAge(7 * 24 * 60 * 60);
            cookie1.setMaxAge(7 * 24 * 60 * 60);
            cookie2.setMaxAge(7 * 24 * 60 * 60);
        }
        response.addCookie(cookie);
        response.addCookie(cookie1);
        response.addCookie(cookie2);
    }
}

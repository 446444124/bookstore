package com.PTU.controller;

import com.PTU.constant.JwtClaimsConstant;
import com.PTU.dto.UserLoginDTO;
import com.PTU.entity.User;
import com.PTU.properties.JwtProperties;
import com.PTU.result.Result;
import com.PTU.service.UserService;
import com.PTU.utils.JwtUtil;
import com.PTU.vo.UserLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user/user")
@Api(tags = "C端用户接口")
@Slf4j
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private JwtProperties jwtProperties;
    @PostMapping("/login")
    @ApiOperation("用户登录")
    public Result<UserLoginVO> login(@RequestBody UserLoginDTO userLoginDTO){
        log.info("用户登录:{}",userLoginDTO);
        //登录
        User user = userService.login(userLoginDTO);
       //  生成jwt令牌
        Map<String,Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID,user.getUserId());
        String token = JwtUtil.createJWT(
                jwtProperties.getUserSecretKey(),
                jwtProperties.getUserTtl(),
                claims);
        UserLoginVO userLoginVO = UserLoginVO.builder()
                .id(user.getUserId())
                .token(token)
                .build();
        return Result.success(userLoginVO);
    }

    @ApiOperation("用户退出")
    @PostMapping("/logout")
    public Result<String> logout() {
        return Result.success();
    }

    @PostMapping("/register")
    @ApiOperation("用户注册")
    public Result<UserLoginVO> register(@RequestBody User tuser){
        log.info("用户注册:{}",tuser);
        //注册
        userService.register(tuser);

        return Result.success();
    }

}

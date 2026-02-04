package com.PTU.service;


import com.PTU.dto.UserDTO;
import com.PTU.dto.UserLoginDTO;
import com.PTU.entity.User;
import com.PTU.result.Result;
import com.baomidou.mybatisplus.extension.service.IService;

public interface UserService extends IService<User> {

    User login(UserLoginDTO userLoginDTO);

     Result register(UserDTO tuser);
}

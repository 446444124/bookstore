package com.PTU.service;

import com.PTU.dto.UserChangePasswordDTO;
import com.PTU.result.Result;

public interface UserPasswordChangeService {

    Result changePassword(Long userId, UserChangePasswordDTO dto);
}

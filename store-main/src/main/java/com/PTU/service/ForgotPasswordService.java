package com.PTU.service;

import com.PTU.dto.UserForgotPasswordResetDTO;
import com.PTU.dto.UserForgotPasswordSendCodeDTO;
import com.PTU.result.Result;

public interface ForgotPasswordService {

    Result sendResetCode(UserForgotPasswordSendCodeDTO dto);

    Result<Void> resetPasswordByEmail(UserForgotPasswordResetDTO dto);
}

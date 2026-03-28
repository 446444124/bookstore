package com.PTU.service;

import com.PTU.dto.UserRegisterSendEmailCodeDTO;
import com.PTU.result.Result;

public interface RegisterEmailCodeService {

    Result sendRegisterEmailCode(UserRegisterSendEmailCodeDTO dto);

    /**
     * @return null 表示通过；否则为错误文案
     */
    String verifyAndConsumeRegisterEmailCode(String email, String code);
}

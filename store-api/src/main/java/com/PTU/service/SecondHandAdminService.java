package com.PTU.service;

import com.PTU.dto.SecondHandEvaluateDTO;
import com.PTU.result.PageResult;

public interface SecondHandAdminService {

    PageResult page(int page, int pageSize, Integer status);

    /** 待店员评估（待审核）条数，用于侧栏徽标 */
    long pendingEvaluateCount();

    void evaluate(SecondHandEvaluateDTO dto);
}

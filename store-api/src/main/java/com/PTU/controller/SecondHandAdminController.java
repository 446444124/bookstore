package com.PTU.controller;

import com.PTU.dto.SecondHandEvaluateDTO;
import com.PTU.result.PageResult;
import com.PTU.result.Result;
import com.PTU.service.SecondHandAdminService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/secondHand")
@Api(tags = "店员-二手书回收审核")
@Slf4j
public class SecondHandAdminController {

    @Autowired
    private SecondHandAdminService secondHandAdminService;

    @GetMapping("/page")
    @ApiOperation("二手书回收分页")
    public Result<PageResult> page(@RequestParam(defaultValue = "1") int page,
                                   @RequestParam(defaultValue = "10") int pageSize,
                                   @RequestParam(required = false) Integer status) {
        return Result.success(secondHandAdminService.page(page, pageSize, status));
    }

    @GetMapping("/pendingCount")
    @ApiOperation("待评估（待审核）条数，用于侧栏徽标")
    public Result<Long> pendingCount() {
        return Result.success(secondHandAdminService.pendingEvaluateCount());
    }

    @PostMapping("/evaluate")
    @ApiOperation("评估：同意上架（选定成色自动算价）或驳回")
    public Result<Void> evaluate(@RequestBody SecondHandEvaluateDTO dto) {
        log.info("二手书审核：{}", dto);
        secondHandAdminService.evaluate(dto);
        return Result.success();
    }
}

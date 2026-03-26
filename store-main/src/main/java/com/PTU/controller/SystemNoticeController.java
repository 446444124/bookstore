package com.PTU.controller;

import com.PTU.result.Result;
import com.PTU.service.SystemNoticeService;
import com.PTU.vo.SystemNoticeVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/systemNotice")
@Api(tags = "用户端-系统公告")
public class SystemNoticeController {

    @Autowired
    private SystemNoticeService systemNoticeService;

    @GetMapping("/active")
    @ApiOperation("获取当前启用公告（未启用返回 null）")
    public Result<SystemNoticeVO> active() {
        return Result.success(systemNoticeService.getActive());
    }
}


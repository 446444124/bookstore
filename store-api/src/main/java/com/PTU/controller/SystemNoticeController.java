package com.PTU.controller;

import com.PTU.dto.SystemNoticeSaveDTO;
import com.PTU.result.Result;
import com.PTU.service.SystemNoticeService;
import com.PTU.vo.SystemNoticeVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/systemNotice")
@Api(tags = "店员-系统公告")
@Slf4j
public class SystemNoticeController {

    @Autowired
    private SystemNoticeService systemNoticeService;

    @GetMapping
    @ApiOperation("获取系统公告配置")
    public Result<SystemNoticeVO> get() {
        return Result.success(systemNoticeService.getConfig());
    }

    @PutMapping
    @ApiOperation("更新系统公告配置")
    public Result<Void> save(@RequestBody SystemNoticeSaveDTO dto) {
        systemNoticeService.save(dto);
        return Result.success();
    }
}


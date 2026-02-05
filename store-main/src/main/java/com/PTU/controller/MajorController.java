package com.PTU.controller;

import com.PTU.dto.MajorPageQueryDTO;
import com.PTU.result.PageResult;
import com.PTU.result.Result;
import com.PTU.service.MajorService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/major")
@Api(tags = "C端用户接口")
@Slf4j
public class MajorController {
    @Autowired
    private MajorService majorService;



    @ApiOperation("专业专业分页查询")
    @GetMapping("/page")
    public Result<PageResult> page(MajorPageQueryDTO majorPageQueryDTO){
        PageResult pageResult =majorService.pageQuery(majorPageQueryDTO);
        return Result.success(pageResult);
    }
}

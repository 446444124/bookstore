package com.PTU.controller;

import com.PTU.result.Result;
import com.PTU.service.SpecialOfferService;
import com.PTU.vo.SpecialOfferVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user/specialOffer")
@Api(tags = "用户端-特惠专区")
public class SpecialOfferController {

    @Autowired
    private SpecialOfferService specialOfferService;

    @GetMapping("/list")
    @ApiOperation("特惠活动列表（仅启用且有效期内）")
    public Result<List<SpecialOfferVO>> list() {
        return Result.success(specialOfferService.listActive());
    }
}


package com.PTU.controller;

import com.PTU.dto.SpecialOfferSaveDTO;
import com.PTU.result.Result;
import com.PTU.service.SpecialOfferService;
import com.PTU.vo.SpecialOfferVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/specialOffer")
@Api(tags = "店员-特惠图书配置")
@Slf4j
public class SpecialOfferController {

    @Autowired
    private SpecialOfferService specialOfferService;

    @GetMapping("/list")
    @ApiOperation("特惠活动列表（全部）")
    public Result<List<SpecialOfferVO>> list() {
        return Result.success(specialOfferService.listAll());
    }

    @PostMapping
    @ApiOperation("新增特惠活动")
    public Result<Void> create(@RequestBody SpecialOfferSaveDTO dto) {
        specialOfferService.create(dto);
        return Result.success();
    }

    @PutMapping("/{id}")
    @ApiOperation("更新特惠活动")
    public Result<Void> update(@PathVariable Long id, @RequestBody SpecialOfferSaveDTO dto) {
        specialOfferService.update(id, dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除特惠活动")
    public Result<Void> delete(@PathVariable Long id) {
        specialOfferService.delete(id);
        return Result.success();
    }

    @PutMapping("/{id}/enable")
    @ApiOperation("启用/停用")
    public Result<Void> enable(@PathVariable Long id, @RequestParam boolean enabled) {
        specialOfferService.enable(id, enabled);
        return Result.success();
    }
}


package com.PTU.controller;

import com.PTU.dto.CarouselBannerSaveDTO;
import com.PTU.result.Result;
import com.PTU.service.CarouselBannerService;
import com.PTU.vo.CarouselBannerVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/carouselBanner")
@Api(tags = "店员-轮播图配置")
@Slf4j
public class CarouselBannerController {

    @Autowired
    private CarouselBannerService carouselBannerService;

    @GetMapping("/list")
    @ApiOperation("轮播图列表（全部）")
    public Result<List<CarouselBannerVO>> list() {
        return Result.success(carouselBannerService.listAll());
    }

    @PostMapping
    @ApiOperation("新增轮播图")
    public Result<Void> create(@RequestBody CarouselBannerSaveDTO dto) {
        carouselBannerService.create(dto);
        return Result.success();
    }

    @PutMapping("/{id}")
    @ApiOperation("更新轮播图")
    public Result<Void> update(@PathVariable Long id, @RequestBody CarouselBannerSaveDTO dto) {
        carouselBannerService.update(id, dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除轮播图")
    public Result<Void> delete(@PathVariable Long id) {
        carouselBannerService.delete(id);
        return Result.success();
    }

    @PutMapping("/{id}/enable")
    @ApiOperation("启用/停用")
    public Result<Void> enable(@PathVariable Long id, @RequestParam boolean enabled) {
        carouselBannerService.enable(id, enabled);
        return Result.success();
    }
}


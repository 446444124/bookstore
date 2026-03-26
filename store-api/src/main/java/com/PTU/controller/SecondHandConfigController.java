package com.PTU.controller;

import com.PTU.dto.SecondHandConfigSaveDTO;
import com.PTU.dto.SecondHandGradeSaveDTO;
import com.PTU.result.Result;
import com.PTU.service.SecondHandConfigService;
import com.PTU.vo.SecondHandConfigVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/secondHand/config")
@Api(tags = "店员-二手书回收配置")
@Slf4j
public class SecondHandConfigController {

    @Autowired
    private SecondHandConfigService secondHandConfigService;

    @GetMapping
    @ApiOperation("获取二手书回收配置（服务费 + 档位）")
    public Result<SecondHandConfigVO> get() {
        return Result.success(secondHandConfigService.getConfig());
    }

    @PutMapping("/serviceFee")
    @ApiOperation("更新服务费百分比")
    public Result<Void> saveServiceFee(@RequestBody SecondHandConfigSaveDTO dto) {
        secondHandConfigService.saveServiceFee(dto);
        return Result.success();
    }

    @PostMapping("/grades")
    @ApiOperation("新增成色档位")
    public Result<Void> createGrade(@RequestBody SecondHandGradeSaveDTO dto) {
        secondHandConfigService.createGrade(dto);
        return Result.success();
    }

    @PutMapping("/grades/{id}")
    @ApiOperation("编辑成色档位")
    public Result<Void> updateGrade(@PathVariable Long id, @RequestBody SecondHandGradeSaveDTO dto) {
        secondHandConfigService.updateGrade(id, dto);
        return Result.success();
    }

    @PutMapping("/grades/{id}/enable")
    @ApiOperation("启用/禁用成色档位")
    public Result<Void> enableGrade(@PathVariable Long id, @RequestParam boolean enabled) {
        secondHandConfigService.enableGrade(id, enabled);
        return Result.success();
    }

    @DeleteMapping("/grades/{id}")
    @ApiOperation("删除成色档位（若已被条目使用则拒绝）")
    public Result<Void> deleteGrade(@PathVariable Long id) {
        secondHandConfigService.deleteGrade(id);
        return Result.success();
    }
}


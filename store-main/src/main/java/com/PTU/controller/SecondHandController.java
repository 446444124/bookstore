package com.PTU.controller;

import com.PTU.dto.SecondHandSubmitDTO;
import com.PTU.result.PageResult;
import com.PTU.result.Result;
import com.PTU.service.SecondHandListingService;
import com.PTU.vo.SecondHandListingVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/secondHand")
@Api(tags = "二手书回收与选购")
@Slf4j
public class SecondHandController {

    @Autowired
    private SecondHandListingService secondHandListingService;

    @GetMapping("/onSale")
    @ApiOperation("在售二手书分页（无需登录）")
    public Result<PageResult> onSale(@RequestParam(defaultValue = "1") int page,
                                     @RequestParam(defaultValue = "10") int pageSize,
                                     @RequestParam(required = false) String title) {
        return Result.success(secondHandListingService.pageOnSale(page, pageSize, title));
    }

    @GetMapping("/detail/{id}")
    @ApiOperation("二手书详情（在售，无需登录）")
    public Result<SecondHandListingVO> detail(@PathVariable Long id) {
        return Result.success(secondHandListingService.getOnSaleDetail(id));
    }

    @PostMapping("/submit")
    @ApiOperation("提交回收申请（需登录，须为本店在售图书）")
    public Result<Void> submit(@RequestBody SecondHandSubmitDTO dto) {
        secondHandListingService.submitListing(dto);
        return Result.success();
    }

    @GetMapping("/my")
    @ApiOperation("我的回收记录")
    public Result<PageResult> my(@RequestParam(defaultValue = "1") int page,
                                 @RequestParam(defaultValue = "10") int pageSize,
                                 @RequestParam(required = false) Integer status) {
        return Result.success(secondHandListingService.pageMy(page, pageSize, status));
    }

    @PutMapping("/withdraw/{id}")
    @ApiOperation("撤回待审核申请")
    public Result<Void> withdraw(@PathVariable Long id) {
        secondHandListingService.withdraw(id);
        return Result.success();
    }
}

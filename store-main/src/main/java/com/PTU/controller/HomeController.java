package com.PTU.controller;

import com.PTU.context.BaseContext;
import com.PTU.result.Result;
import com.PTU.service.HomeRecommendService;
import com.PTU.vo.HomeRecommendVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/home")
@Api(tags = "首页推荐接口")
public class HomeController {

    @Autowired
    private HomeRecommendService homeRecommendService;

    @GetMapping("/recommend")
    @ApiOperation("首页个性化推荐")
    public Result<HomeRecommendVO> recommend(@RequestParam(required = false, defaultValue = "10") Integer categoryLimit,
                                             @RequestParam(required = false, defaultValue = "12") Integer bookLimit) {
        Long userId = BaseContext.getCurrentId();
        HomeRecommendVO vo = homeRecommendService.recommendForHome(userId, categoryLimit, bookLimit);
        return Result.success(vo);
    }
}

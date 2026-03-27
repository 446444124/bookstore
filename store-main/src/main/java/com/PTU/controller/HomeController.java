package com.PTU.controller;

import com.PTU.context.BaseContext;
import com.PTU.result.Result;
import com.PTU.service.CarouselBannerService;
import com.PTU.service.HomeRecommendService;
import com.PTU.vo.CarouselBannerVO;
import com.PTU.vo.HomeRecommendVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user/home")
@Api(tags = "首页推荐接口")
public class HomeController {

    @Autowired
    private HomeRecommendService homeRecommendService;

    @Autowired
    private CarouselBannerService carouselBannerService;

    @GetMapping("/recommend")
    @ApiOperation("首页个性化推荐")
    public Result<HomeRecommendVO> recommend(@RequestParam(required = false, defaultValue = "10") Integer categoryLimit,
                                             @RequestParam(required = false, defaultValue = "12") Integer bookLimit) {
        Long userId = BaseContext.getCurrentId();
        HomeRecommendVO vo = homeRecommendService.recommendForHome(userId, categoryLimit, bookLimit);
        return Result.success(vo);
    }

    @GetMapping("/banners")
    @ApiOperation("首页轮播图（启用项，按排序）")
    public Result<List<CarouselBannerVO>> banners() {
        return Result.success(carouselBannerService.listEnabled());
    }
}

package com.PTU.service.impl;

import com.PTU.entity.CarouselBanner;
import com.PTU.mapper.CarouselBannerMapper;
import com.PTU.service.CarouselBannerService;
import com.PTU.vo.CarouselBannerVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CarouselBannerServiceImpl implements CarouselBannerService {

    @Autowired
    private CarouselBannerMapper carouselBannerMapper;

    @Override
    public List<CarouselBannerVO> listEnabled() {
        List<CarouselBanner> rows = carouselBannerMapper.selectList(
                new LambdaQueryWrapper<CarouselBanner>()
                        .eq(CarouselBanner::getEnabled, 1)
                        .orderByAsc(CarouselBanner::getSort)
                        .orderByDesc(CarouselBanner::getId)
        );
        if (rows == null || rows.isEmpty()) return new ArrayList<>();
        List<CarouselBannerVO> out = new ArrayList<>(rows.size());
        for (CarouselBanner r : rows) {
            CarouselBannerVO vo = new CarouselBannerVO();
            BeanUtils.copyProperties(r, vo);
            out.add(vo);
        }
        return out;
    }
}


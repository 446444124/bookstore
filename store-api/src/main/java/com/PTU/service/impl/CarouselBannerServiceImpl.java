package com.PTU.service.impl;

import com.PTU.context.BaseContext;
import com.PTU.dto.CarouselBannerSaveDTO;
import com.PTU.entity.CarouselBanner;
import com.PTU.exception.BaseException;
import com.PTU.mapper.CarouselBannerMapper;
import com.PTU.service.CarouselBannerService;
import com.PTU.vo.CarouselBannerVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CarouselBannerServiceImpl implements CarouselBannerService {

    @Autowired
    private CarouselBannerMapper carouselBannerMapper;

    @Override
    public List<CarouselBannerVO> listAll() {
        List<CarouselBanner> rows = carouselBannerMapper.selectList(
                new LambdaQueryWrapper<CarouselBanner>()
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

    @Override
    @Transactional
    public void create(CarouselBannerSaveDTO dto) {
        CarouselBanner row = buildAndValidate(null, dto);
        row.setCreateTime(LocalDateTime.now());
        row.setUpdateTime(LocalDateTime.now());
        row.setUpdateBy(BaseContext.getCurrentId());
        carouselBannerMapper.insert(row);
    }

    @Override
    @Transactional
    public void update(Long id, CarouselBannerSaveDTO dto) {
        if (id == null) throw new BaseException("参数错误");
        CarouselBanner exists = carouselBannerMapper.selectById(id);
        if (exists == null) throw new BaseException("轮播图不存在");
        CarouselBanner row = buildAndValidate(id, dto);
        row.setUpdateTime(LocalDateTime.now());
        row.setUpdateBy(BaseContext.getCurrentId());
        carouselBannerMapper.updateById(row);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (id == null) throw new BaseException("参数错误");
        carouselBannerMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void enable(Long id, boolean enabled) {
        if (id == null) throw new BaseException("参数错误");
        CarouselBanner exists = carouselBannerMapper.selectById(id);
        if (exists == null) throw new BaseException("轮播图不存在");
        carouselBannerMapper.updateById(CarouselBanner.builder()
                .id(id)
                .enabled(enabled ? 1 : 0)
                .updateTime(LocalDateTime.now())
                .updateBy(BaseContext.getCurrentId())
                .build());
    }

    private CarouselBanner buildAndValidate(Long id, CarouselBannerSaveDTO dto) {
        if (dto == null) throw new BaseException("参数错误");
        String imageUrl = dto.getImageUrl() == null ? "" : dto.getImageUrl().trim();
        if (imageUrl.isEmpty()) throw new BaseException("图片地址不能为空");
        if (imageUrl.length() > 512) throw new BaseException("图片地址长度不能超过 512");
        String linkPath = dto.getLinkPath() == null ? null : dto.getLinkPath().trim();
        if (linkPath != null && linkPath.isEmpty()) linkPath = null;
        if (linkPath != null && linkPath.length() > 255) throw new BaseException("跳转路径长度不能超过 255");
        Integer enabled = dto.getEnabled();
        if (enabled == null) enabled = 1;
        if (!(enabled == 0 || enabled == 1)) throw new BaseException("enabled 只能为 0 或 1");
        Integer sort = dto.getSort();
        if (sort == null) sort = 0;
        return CarouselBanner.builder()
                .id(id)
                .imageUrl(imageUrl)
                .linkPath(linkPath)
                .enabled(enabled)
                .sort(sort)
                .build();
    }
}


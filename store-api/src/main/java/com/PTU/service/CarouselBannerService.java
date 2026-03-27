package com.PTU.service;

import com.PTU.dto.CarouselBannerSaveDTO;
import com.PTU.vo.CarouselBannerVO;

import java.util.List;

public interface CarouselBannerService {

    List<CarouselBannerVO> listAll();

    void create(CarouselBannerSaveDTO dto);

    void update(Long id, CarouselBannerSaveDTO dto);

    void delete(Long id);

    void enable(Long id, boolean enabled);
}


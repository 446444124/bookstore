package com.PTU.service;

import com.PTU.dto.SpecialOfferSaveDTO;
import com.PTU.vo.SpecialOfferVO;

import java.util.List;

public interface SpecialOfferService {

    List<SpecialOfferVO> listAll();

    void create(SpecialOfferSaveDTO dto);

    void update(Long id, SpecialOfferSaveDTO dto);

    void delete(Long id);

    void enable(Long id, boolean enabled);
}


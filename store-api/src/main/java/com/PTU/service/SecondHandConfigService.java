package com.PTU.service;

import com.PTU.dto.SecondHandConfigSaveDTO;
import com.PTU.dto.SecondHandGradeSaveDTO;
import com.PTU.vo.SecondHandConfigVO;

public interface SecondHandConfigService {

    SecondHandConfigVO getConfig();

    void saveServiceFee(SecondHandConfigSaveDTO dto);

    void createGrade(SecondHandGradeSaveDTO dto);

    void updateGrade(Long id, SecondHandGradeSaveDTO dto);

    void enableGrade(Long id, boolean enabled);

    void deleteGrade(Long id);
}


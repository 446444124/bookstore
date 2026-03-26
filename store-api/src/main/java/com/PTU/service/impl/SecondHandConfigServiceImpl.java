package com.PTU.service.impl;

import com.PTU.context.BaseContext;
import com.PTU.dto.SecondHandConfigSaveDTO;
import com.PTU.dto.SecondHandGradeSaveDTO;
import com.PTU.entity.SecondHandConfig;
import com.PTU.entity.SecondHandGrade;
import com.PTU.entity.SecondHandListing;
import com.PTU.exception.BaseException;
import com.PTU.mapper.SecondHandConfigMapper;
import com.PTU.mapper.SecondHandGradeMapper;
import com.PTU.mapper.SecondHandListingMapper;
import com.PTU.service.SecondHandConfigService;
import com.PTU.vo.SecondHandConfigVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SecondHandConfigServiceImpl implements SecondHandConfigService {

    private static final int CONFIG_ID = 1;

    @Autowired
    private SecondHandConfigMapper secondHandConfigMapper;
    @Autowired
    private SecondHandGradeMapper secondHandGradeMapper;
    @Autowired
    private SecondHandListingMapper secondHandListingMapper;

    @Override
    public SecondHandConfigVO getConfig() {
        SecondHandConfig cfg = ensureConfigRow();
        List<SecondHandGrade> grades = secondHandGradeMapper.selectList(
                new LambdaQueryWrapper<SecondHandGrade>()
                        .orderByAsc(SecondHandGrade::getSort)
                        .orderByAsc(SecondHandGrade::getId));
        return SecondHandConfigVO.builder()
                .serviceFeePercent(cfg.getServiceFeePercent() == null ? BigDecimal.ZERO : cfg.getServiceFeePercent())
                .grades(grades)
                .build();
    }

    @Override
    @Transactional
    public void saveServiceFee(SecondHandConfigSaveDTO dto) {
        BigDecimal v = dto == null ? null : dto.getServiceFeePercent();
        if (v == null) {
            throw new BaseException("缺少服务费百分比");
        }
        if (v.compareTo(BigDecimal.ZERO) < 0 || v.compareTo(new BigDecimal("100")) > 0) {
            throw new BaseException("服务费百分比范围应为 0-100");
        }
        ensureConfigRow();
        secondHandConfigMapper.updateById(SecondHandConfig.builder()
                .id(CONFIG_ID)
                .serviceFeePercent(v)
                .updateBy(BaseContext.getCurrentId())
                .updateTime(LocalDateTime.now())
                .build());
    }

    @Override
    @Transactional
    public void createGrade(SecondHandGradeSaveDTO dto) {
        if (dto == null) throw new BaseException("参数错误");
        if (dto.getName() == null || dto.getName().trim().isEmpty()) throw new BaseException("档位名称不能为空");
        if (dto.getRecyclePercent() == null) throw new BaseException("回收百分比不能为空");
        if (dto.getRecyclePercent().compareTo(BigDecimal.ZERO) < 0 || dto.getRecyclePercent().compareTo(new BigDecimal("100")) > 0) {
            throw new BaseException("回收百分比范围应为 0-100");
        }
        SecondHandGrade row = new SecondHandGrade();
        BeanUtils.copyProperties(dto, row);
        row.setId(null);
        if (row.getEnabled() == null) row.setEnabled(1);
        if (row.getSort() == null) row.setSort(0);
        row.setCreateTime(LocalDateTime.now());
        row.setUpdateTime(LocalDateTime.now());
        secondHandGradeMapper.insert(row);
    }

    @Override
    @Transactional
    public void updateGrade(Long id, SecondHandGradeSaveDTO dto) {
        if (id == null) throw new BaseException("缺少档位ID");
        if (dto == null) throw new BaseException("参数错误");
        SecondHandGrade old = secondHandGradeMapper.selectById(id);
        if (old == null) throw new BaseException("档位不存在");
        if (dto.getName() != null && dto.getName().trim().isEmpty()) throw new BaseException("档位名称不能为空");
        if (dto.getRecyclePercent() != null) {
            if (dto.getRecyclePercent().compareTo(BigDecimal.ZERO) < 0 || dto.getRecyclePercent().compareTo(new BigDecimal("100")) > 0) {
                throw new BaseException("回收百分比范围应为 0-100");
            }
        }
        SecondHandGrade upd = new SecondHandGrade();
        upd.setId(id);
        if (dto.getName() != null) upd.setName(dto.getName().trim());
        if (dto.getRecyclePercent() != null) upd.setRecyclePercent(dto.getRecyclePercent());
        if (dto.getEnabled() != null) upd.setEnabled(dto.getEnabled());
        if (dto.getSort() != null) upd.setSort(dto.getSort());
        upd.setUpdateTime(LocalDateTime.now());
        secondHandGradeMapper.updateById(upd);
    }

    @Override
    @Transactional
    public void enableGrade(Long id, boolean enabled) {
        if (id == null) throw new BaseException("缺少档位ID");
        SecondHandGrade old = secondHandGradeMapper.selectById(id);
        if (old == null) throw new BaseException("档位不存在");
        secondHandGradeMapper.updateById(SecondHandGrade.builder()
                .id(id)
                .enabled(enabled ? 1 : 0)
                .updateTime(LocalDateTime.now())
                .build());
    }

    @Override
    @Transactional
    public void deleteGrade(Long id) {
        if (id == null) throw new BaseException("缺少档位ID");
        long used = secondHandListingMapper.selectCount(new LambdaQueryWrapper<SecondHandListing>()
                .eq(SecondHandListing::getGradeId, id));
        if (used > 0) {
            throw new BaseException("该档位已被条目使用，无法删除");
        }
        secondHandGradeMapper.deleteById(id);
    }

    private SecondHandConfig ensureConfigRow() {
        SecondHandConfig cfg = secondHandConfigMapper.selectById(CONFIG_ID);
        if (cfg != null) return cfg;
        SecondHandConfig init = SecondHandConfig.builder()
                .id(CONFIG_ID)
                .serviceFeePercent(BigDecimal.ZERO)
                .updateTime(LocalDateTime.now())
                .updateBy(BaseContext.getCurrentId())
                .build();
        try {
            secondHandConfigMapper.insert(init);
        } catch (Exception ignore) {
        }
        SecondHandConfig again = secondHandConfigMapper.selectById(CONFIG_ID);
        return again != null ? again : init;
    }
}


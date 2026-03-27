package com.PTU.service.impl;

import com.PTU.context.BaseContext;
import com.PTU.dto.SpecialOfferSaveDTO;
import com.PTU.entity.Book;
import com.PTU.entity.SpecialOffer;
import com.PTU.entity.SpecialOfferItem;
import com.PTU.exception.BaseException;
import com.PTU.mapper.BookMapper;
import com.PTU.mapper.SpecialOfferItemMapper;
import com.PTU.mapper.SpecialOfferMapper;
import com.PTU.service.SpecialOfferService;
import com.PTU.vo.SpecialOfferVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class SpecialOfferServiceImpl implements SpecialOfferService {

    @Autowired
    private SpecialOfferMapper specialOfferMapper;
    @Autowired
    private SpecialOfferItemMapper specialOfferItemMapper;
    @Autowired
    private BookMapper bookMapper;

    @Override
    public List<SpecialOfferVO> listAll() {
        List<SpecialOffer> offers = specialOfferMapper.selectList(
                new LambdaQueryWrapper<SpecialOffer>()
                        .orderByAsc(SpecialOffer::getSort)
                        .orderByDesc(SpecialOffer::getId)
        );
        if (offers == null || offers.isEmpty()) return new ArrayList<>();
        List<SpecialOfferItem> items = specialOfferItemMapper.selectList(new LambdaQueryWrapper<>());
        Map<Long, List<SpecialOfferItem>> byOffer = new HashMap<>();
        for (SpecialOfferItem it : items) {
            byOffer.computeIfAbsent(it.getOfferId(), k -> new ArrayList<>()).add(it);
        }
        List<SpecialOfferVO> out = new ArrayList<>(offers.size());
        for (SpecialOffer o : offers) {
            out.add(toVO(o, byOffer.getOrDefault(o.getId(), Collections.emptyList())));
        }
        return out;
    }

    @Override
    @Transactional
    public void create(SpecialOfferSaveDTO dto) {
        SpecialOffer row = buildAndValidate(null, dto);
        LocalDateTime now = LocalDateTime.now();
        row.setCreateTime(now);
        row.setUpdateTime(now);
        row.setUpdateBy(BaseContext.getCurrentId());
        specialOfferMapper.insert(row);
        replaceItems(row.getId(), dto.getItems(), row.getOfferType());
    }

    @Override
    @Transactional
    public void update(Long id, SpecialOfferSaveDTO dto) {
        if (id == null) throw new BaseException("参数错误");
        SpecialOffer exists = specialOfferMapper.selectById(id);
        if (exists == null) throw new BaseException("特惠活动不存在");
        SpecialOffer row = buildAndValidate(id, dto);
        row.setUpdateTime(LocalDateTime.now());
        row.setUpdateBy(BaseContext.getCurrentId());
        specialOfferMapper.updateById(row);
        replaceItems(id, dto.getItems(), row.getOfferType());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (id == null) throw new BaseException("参数错误");
        specialOfferItemMapper.delete(new LambdaQueryWrapper<SpecialOfferItem>().eq(SpecialOfferItem::getOfferId, id));
        specialOfferMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void enable(Long id, boolean enabled) {
        if (id == null) throw new BaseException("参数错误");
        SpecialOffer exists = specialOfferMapper.selectById(id);
        if (exists == null) throw new BaseException("特惠活动不存在");
        specialOfferMapper.updateById(SpecialOffer.builder()
                .id(id)
                .enabled(enabled ? 1 : 0)
                .updateTime(LocalDateTime.now())
                .updateBy(BaseContext.getCurrentId())
                .build());
    }

    private void replaceItems(Long offerId, List<SpecialOfferSaveDTO.Item> items, Integer offerType) {
        specialOfferItemMapper.delete(new LambdaQueryWrapper<SpecialOfferItem>().eq(SpecialOfferItem::getOfferId, offerId));
        if (items == null || items.isEmpty()) throw new BaseException("请配置活动包含的图书");
        int idx = 0;
        for (SpecialOfferSaveDTO.Item it : items) {
            idx++;
            if (it == null || it.getBookId() == null) throw new BaseException("第" + idx + "个图书缺少 bookId");
            int qty = it.getQuantity() == null ? 1 : it.getQuantity();
            if (qty < 1) throw new BaseException("第" + idx + "个图书数量必须 >= 1");
            if (offerType != null && offerType == 1) qty = 1; // 单品固定为 1
            specialOfferItemMapper.insert(SpecialOfferItem.builder()
                    .offerId(offerId)
                    .bookId(it.getBookId())
                    .quantity(qty)
                    .build());
        }
    }

    private SpecialOffer buildAndValidate(Long id, SpecialOfferSaveDTO dto) {
        if (dto == null) throw new BaseException("参数错误");
        String name = dto.getName() == null ? "" : dto.getName().trim();
        if (name.isEmpty()) throw new BaseException("活动名称不能为空");
        if (name.length() > 80) throw new BaseException("活动名称长度不能超过 80");
        Integer offerType = dto.getOfferType();
        if (offerType == null || !(offerType == 1 || offerType == 2)) throw new BaseException("offerType 只能为 1 或 2");
        Integer discountType = dto.getDiscountType();
        if (discountType == null || !(discountType == 1 || discountType == 2 || discountType == 3)) {
            throw new BaseException("discountType 只能为 1/2/3");
        }
        BigDecimal discountValue = dto.getDiscountValue();
        if (discountValue == null) throw new BaseException("discountValue 不能为空");
        if (discountValue.compareTo(BigDecimal.ZERO) <= 0) throw new BaseException("discountValue 必须 > 0");
        if (discountType == 1 && discountValue.compareTo(new BigDecimal("100")) > 0) {
            throw new BaseException("折扣百分比不能超过 100");
        }
        Integer enabled = dto.getEnabled();
        if (enabled == null) enabled = 1;
        if (!(enabled == 0 || enabled == 1)) throw new BaseException("enabled 只能为 0 或 1");
        Integer sort = dto.getSort();
        if (sort == null) sort = 0;
        return SpecialOffer.builder()
                .id(id)
                .name(name)
                .offerType(offerType)
                .discountType(discountType)
                .discountValue(discountValue)
                .enabled(enabled)
                .sort(sort)
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .build();
    }

    private SpecialOfferVO toVO(SpecialOffer o, List<SpecialOfferItem> items) {
        SpecialOfferVO vo = new SpecialOfferVO();
        BeanUtils.copyProperties(o, vo);
        List<SpecialOfferVO.Item> its = new ArrayList<>();
        BigDecimal original = BigDecimal.ZERO;
        for (SpecialOfferItem it : items) {
            Book b = bookMapper.selectById(it.getBookId());
            BigDecimal unit = b != null && b.getPrice() != null ? b.getPrice() : BigDecimal.ZERO;
            int q = it.getQuantity() == null ? 1 : it.getQuantity();
            BigDecimal line = unit.multiply(BigDecimal.valueOf(q));
            original = original.add(line);
            its.add(SpecialOfferVO.Item.builder()
                    .bookId(it.getBookId())
                    .title(b != null ? b.getTitle() : "")
                    .coverImage(b != null ? b.getCoverImage() : "")
                    .unitPrice(unit)
                    .quantity(q)
                    .lineAmount(line)
                    .build());
        }
        vo.setItems(its);
        vo.setOriginalAmount(original);
        vo.setDealAmount(applyDiscount(original, o.getDiscountType(), o.getDiscountValue()));
        return vo;
    }

    private BigDecimal applyDiscount(BigDecimal original, Integer discountType, BigDecimal discountValue) {
        if (original == null) original = BigDecimal.ZERO;
        if (discountType == null || discountValue == null) return original;
        if (discountType == 1) {
            return original.multiply(discountValue).divide(new BigDecimal("100"), 2, BigDecimal.ROUND_HALF_UP);
        }
        if (discountType == 2) {
            return discountValue.setScale(2, BigDecimal.ROUND_HALF_UP);
        }
        if (discountType == 3) {
            BigDecimal v = original.subtract(discountValue);
            return v.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : v.setScale(2, BigDecimal.ROUND_HALF_UP);
        }
        return original;
    }
}


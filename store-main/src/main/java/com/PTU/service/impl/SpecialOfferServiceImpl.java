package com.PTU.service.impl;

import com.PTU.entity.Book;
import com.PTU.entity.SpecialOffer;
import com.PTU.entity.SpecialOfferItem;
import com.PTU.mapper.BookMapper;
import com.PTU.mapper.SpecialOfferItemMapper;
import com.PTU.mapper.SpecialOfferMapper;
import com.PTU.service.SpecialOfferService;
import com.PTU.vo.SpecialOfferVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    public List<SpecialOfferVO> listActive() {
        LocalDateTime now = LocalDateTime.now();
        List<SpecialOffer> offers = specialOfferMapper.selectList(
                new LambdaQueryWrapper<SpecialOffer>()
                        .eq(SpecialOffer::getEnabled, 1)
                        .and(q -> q.isNull(SpecialOffer::getStartTime).or().le(SpecialOffer::getStartTime, now))
                        .and(q -> q.isNull(SpecialOffer::getEndTime).or().ge(SpecialOffer::getEndTime, now))
                        .orderByAsc(SpecialOffer::getSort)
                        .orderByDesc(SpecialOffer::getId)
        );
        if (offers == null || offers.isEmpty()) return new ArrayList<>();
        List<Long> ids = new ArrayList<>();
        for (SpecialOffer o : offers) ids.add(o.getId());
        List<SpecialOfferItem> items = specialOfferItemMapper.selectList(
                new LambdaQueryWrapper<SpecialOfferItem>().in(SpecialOfferItem::getOfferId, ids)
        );
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


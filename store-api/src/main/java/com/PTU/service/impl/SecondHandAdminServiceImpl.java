package com.PTU.service.impl;

import com.PTU.constant.SecondHandConstants;
import com.PTU.dto.SecondHandEvaluateDTO;
import com.PTU.entity.Book;
import com.PTU.entity.SecondHandConfig;
import com.PTU.entity.SecondHandGrade;
import com.PTU.entity.SecondHandListing;
import com.PTU.entity.WalletFlow;
import com.PTU.exception.BaseException;
import com.PTU.mapper.BookMapper;
import com.PTU.mapper.SecondHandConfigMapper;
import com.PTU.mapper.SecondHandGradeMapper;
import com.PTU.mapper.SecondHandListingMapper;
import com.PTU.mapper.UserMapper;
import com.PTU.mapper.WalletFlowMapper;
import com.PTU.result.PageResult;
import com.PTU.service.SecondHandAdminService;
import com.PTU.utils.SecondHandListingImageJson;
import com.PTU.vo.SecondHandListingVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SecondHandAdminServiceImpl implements SecondHandAdminService {

    @Autowired
    private SecondHandListingMapper secondHandListingMapper;
    @Autowired
    private BookMapper bookMapper;
    @Autowired
    private SecondHandConfigMapper secondHandConfigMapper;
    @Autowired
    private SecondHandGradeMapper secondHandGradeMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WalletFlowMapper walletFlowMapper;

    @Override
    public PageResult page(int page, int pageSize, Integer status) {
        Page<SecondHandListing> p = new Page<>(page, pageSize);
        LambdaQueryWrapper<SecondHandListing> qw = new LambdaQueryWrapper<>();
        if (status != null) {
            qw.eq(SecondHandListing::getStatus, status);
        }
        qw.orderByDesc(SecondHandListing::getCreateTime);
        secondHandListingMapper.selectPage(p, qw);
        List<SecondHandListingVO> vos = toVoList(p.getRecords());
        return new PageResult(p.getTotal(), vos);
    }

    @Override
    public long pendingEvaluateCount() {
        return secondHandListingMapper.selectCount(
                new LambdaQueryWrapper<SecondHandListing>()
                        .eq(SecondHandListing::getStatus, SecondHandConstants.STATUS_PENDING));
    }

    @Override
    @Transactional
    public void evaluate(SecondHandEvaluateDTO dto) {
        if (dto.getId() == null) {
            throw new BaseException("缺少条目ID");
        }
        SecondHandListing l = secondHandListingMapper.selectById(dto.getId());
        if (l == null || l.getStatus() == null || l.getStatus() != SecondHandConstants.STATUS_PENDING) {
            throw new BaseException("当前记录不在待审核状态");
        }
        if (!Boolean.TRUE.equals(dto.getApprove())) {
            SecondHandListing upd = SecondHandListing.builder()
                    .id(l.getId())
                    .status(SecondHandConstants.STATUS_REJECTED)
                    .staffRemark(dto.getStaffRemark())
                    .updateTime(LocalDateTime.now())
                    .build();
            secondHandListingMapper.updateById(upd);
            return;
        }
        Book book = bookMapper.selectById(l.getBookId());
        if (book == null || book.getStatus() == null || book.getStatus() != 1) {
            throw new BaseException("对应图书非本店在售，无法同意上架");
        }
        SecondHandGrade grade = resolveGrade(dto);
        BigDecimal recyclePercent = grade.getRecyclePercent();
        if (recyclePercent == null) {
            throw new BaseException("成色档位回收比例异常");
        }
        BigDecimal bookPrice = book.getPrice();
        if (bookPrice == null || bookPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BaseException("图书原价异常");
        }
        BigDecimal recyclePrice = bookPrice
                .multiply(recyclePercent)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal serviceFeePercent = ensureServiceFeePercent();
        BigDecimal sale = recyclePrice
                .multiply(new BigDecimal("100").add(serviceFeePercent))
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        Integer legacyRatioInt = null;
        try {
            legacyRatioInt = recyclePercent.setScale(0, RoundingMode.HALF_UP).intValueExact();
        } catch (Exception ignore) {
        }
        SecondHandListing upd = SecondHandListing.builder()
                .id(l.getId())
                .gradeId(grade.getId())
                .gradeName(grade.getName())
                .conditionGrade(dto.getConditionGrade())
                .priceRatio(legacyRatioInt)
                .refBookPrice(bookPrice)
                .recyclePrice(recyclePrice)
                .serviceFeePercent(serviceFeePercent)
                .salePrice(sale)
                .status(SecondHandConstants.STATUS_ON_SALE)
                .staffRemark(dto.getStaffRemark())
                .updateTime(LocalDateTime.now())
                .build();
        secondHandListingMapper.updateById(upd);

        // 中间商模式：审核通过即视为平台收购，平台向提交用户打款（与后续买家支付/退款解耦）。
        payoutSellerOnApprovedListing(l.getId(), l.getSellerUserId(), recyclePrice);
    }

    private SecondHandGrade resolveGrade(SecondHandEvaluateDTO dto) {
        if (dto == null) {
            throw new BaseException("参数错误");
        }
        Long gradeId = dto.getGradeId();
        // 兼容旧前端：conditionGrade 1-4 视为默认档位 id 1-4
        if (gradeId == null && dto.getConditionGrade() != null) {
            gradeId = Long.valueOf(dto.getConditionGrade());
        }
        if (gradeId == null) {
            throw new BaseException("上架时请选定成色档位");
        }
        SecondHandGrade g = secondHandGradeMapper.selectById(gradeId);
        if (g == null) {
            throw new BaseException("成色档位不存在");
        }
        if (g.getEnabled() != null && g.getEnabled() == 0) {
            throw new BaseException("该成色档位已停用");
        }
        if (g.getName() == null || g.getName().trim().isEmpty()) {
            throw new BaseException("成色档位名称异常");
        }
        return g;
    }

    private BigDecimal ensureServiceFeePercent() {
        SecondHandConfig cfg = secondHandConfigMapper.selectById(1);
        if (cfg != null && cfg.getServiceFeePercent() != null) {
            return cfg.getServiceFeePercent();
        }
        // lazy init
        try {
            secondHandConfigMapper.insert(SecondHandConfig.builder()
                    .id(1)
                    .serviceFeePercent(BigDecimal.ZERO)
                    .updateTime(LocalDateTime.now())
                    .build());
        } catch (Exception ignore) {
        }
        SecondHandConfig again = secondHandConfigMapper.selectById(1);
        return (again == null || again.getServiceFeePercent() == null) ? BigDecimal.ZERO : again.getServiceFeePercent();
    }

    private void payoutSellerOnApprovedListing(Long listingId, Long sellerUserId, BigDecimal amount) {
        if (listingId == null || sellerUserId == null) {
            return;
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        String bizNo = "SH_RECYCLE_" + listingId;
        if (walletFlowMapper.countByBizNoAndType(bizNo, WalletFlow.TYPE_SELLER_INCOME) > 0) {
            return;
        }
        int n = userMapper.addWalletBalance(sellerUserId, amount);
        if (n <= 0) {
            throw new BaseException("回收打款失败：用户不存在或余额更新失败");
        }
        walletFlowMapper.insert(WalletFlow.builder()
                .userId(sellerUserId)
                .flowType(WalletFlow.TYPE_SELLER_INCOME)
                .amount(amount)
                .bizNo(bizNo)
                .remark("二手书回收打款（条目 " + listingId + "）")
                .createTime(LocalDateTime.now())
                .build());
    }

    private List<SecondHandListingVO> toVoList(List<SecondHandListing> rows) {
        if (rows == null || rows.isEmpty()) {
            return new ArrayList<>();
        }
        Set<Long> bookIds = rows.stream().map(SecondHandListing::getBookId).collect(Collectors.toSet());
        List<Book> books = bookMapper.selectList(new LambdaQueryWrapper<Book>().in(Book::getId, bookIds));
        Map<Long, Book> bookMap = books.stream().collect(Collectors.toMap(Book::getId, b -> b, (a, b) -> a));
        List<SecondHandListingVO> list = new ArrayList<>();
        for (SecondHandListing l : rows) {
            list.add(toVo(l, bookMap.get(l.getBookId())));
        }
        return list;
    }

    private SecondHandListingVO toVo(SecondHandListing l, Book b) {
        SecondHandListingVO.SecondHandListingVOBuilder vb = SecondHandListingVO.builder()
                .id(l.getId())
                .bookId(l.getBookId())
                .sellerUserId(l.getSellerUserId())
                .userNote(l.getUserNote())
                .userConditionImages(SecondHandListingImageJson.parse(l.getUserConditionImages()))
                .conditionGrade(l.getConditionGrade())
                .conditionGradeText((l.getGradeName() != null && !l.getGradeName().trim().isEmpty())
                        ? l.getGradeName().trim()
                        : SecondHandConstants.gradeText(l.getConditionGrade()))
                .priceRatio(l.getPriceRatio())
                .refBookPrice(l.getRefBookPrice())
                .salePrice(l.getSalePrice())
                .status(l.getStatus())
                .statusText(SecondHandConstants.statusText(l.getStatus()))
                .staffRemark(l.getStaffRemark())
                .buyerUserId(l.getBuyerUserId())
                .orderId(l.getOrderId())
                .soldTime(l.getSoldTime())
                .createTime(l.getCreateTime());
        if (b != null) {
            vb.bookTitle(b.getTitle())
                    .bookAuthor(b.getAuthor())
                    .coverImage(b.getCoverImage())
                    .bookOriginalPrice(b.getPrice());
        }
        return vb.build();
    }
}

package com.PTU.service.impl;

import com.PTU.constant.SecondHandConstants;
import com.PTU.dto.SecondHandEvaluateDTO;
import com.PTU.entity.Book;
import com.PTU.entity.SecondHandListing;
import com.PTU.exception.BaseException;
import com.PTU.mapper.BookMapper;
import com.PTU.mapper.SecondHandListingMapper;
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
        Integer grade = dto.getConditionGrade();
        if (grade == null || grade < 1 || grade > 4) {
            throw new BaseException("上架时请选定成色（1近新 2良好 3一般 4较差）");
        }
        Book book = bookMapper.selectById(l.getBookId());
        if (book == null || book.getStatus() == null || book.getStatus() != 1) {
            throw new BaseException("对应图书非本店在售，无法同意上架");
        }
        int ratio = SecondHandConstants.ratioPercentForGrade(grade);
        BigDecimal sale = SecondHandConstants.salePrice(book.getPrice(), ratio);
        SecondHandListing upd = SecondHandListing.builder()
                .id(l.getId())
                .conditionGrade(grade)
                .priceRatio(ratio)
                .refBookPrice(book.getPrice())
                .salePrice(sale)
                .status(SecondHandConstants.STATUS_ON_SALE)
                .staffRemark(dto.getStaffRemark())
                .updateTime(LocalDateTime.now())
                .build();
        secondHandListingMapper.updateById(upd);
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
                .conditionGradeText(SecondHandConstants.gradeText(l.getConditionGrade()))
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

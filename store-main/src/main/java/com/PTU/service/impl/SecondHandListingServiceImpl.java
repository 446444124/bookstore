package com.PTU.service.impl;

import com.PTU.constant.MessageConstant;
import com.PTU.constant.SecondHandConstants;
import com.PTU.context.BaseContext;
import com.PTU.dto.SecondHandSubmitDTO;
import com.PTU.entity.Book;
import com.PTU.entity.Orders;
import com.PTU.entity.SecondHandOrder;
import com.PTU.entity.SecondHandListing;
import com.PTU.entity.WalletFlow;
import com.PTU.exception.BaseException;
import com.PTU.mapper.BookMapper;
import com.PTU.mapper.OrderMapper;
import com.PTU.mapper.SecondHandOrderMapper;
import com.PTU.mapper.SecondHandListingMapper;
import com.PTU.mapper.UserMapper;
import com.PTU.mapper.WalletFlowMapper;
import com.PTU.result.PageResult;
import com.PTU.service.SecondHandListingService;
import com.PTU.utils.SecondHandListingImageJson;
import com.PTU.vo.SecondHandListingVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
public class SecondHandListingServiceImpl implements SecondHandListingService {

    @Autowired
    private SecondHandListingMapper secondHandListingMapper;
    @Autowired
    private BookMapper bookMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private SecondHandOrderMapper secondHandOrderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WalletFlowMapper walletFlowMapper;

    @Override
    @Transactional
    public void submitListing(SecondHandSubmitDTO dto) {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new BaseException(MessageConstant.LOGIN_FAILED);
        }
        if (dto.getBookId() == null) {
            throw new BaseException("请选择店内图书");
        }
        Book book = bookMapper.selectById(dto.getBookId());
        if (book == null || book.getStatus() == null || book.getStatus() != 1) {
            throw new BaseException("只能选择本店在售图书");
        }
        SecondHandListing row = SecondHandListing.builder()
                .bookId(dto.getBookId())
                .sellerUserId(userId)
                .userNote(dto.getUserNote())
                .userConditionImages(SecondHandListingImageJson.toJson(dto.getUserConditionImages()))
                .status(SecondHandConstants.STATUS_PENDING)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        secondHandListingMapper.insert(row);
    }

    @Override
    public PageResult pageOnSale(int page, int pageSize, String titleKeyword) {
        Page<SecondHandListing> p = new Page<>(page, pageSize);
        LambdaQueryWrapper<SecondHandListing> qw = new LambdaQueryWrapper<>();
        qw.eq(SecondHandListing::getStatus, SecondHandConstants.STATUS_ON_SALE)
                .orderByDesc(SecondHandListing::getUpdateTime);
        if (titleKeyword != null && !titleKeyword.trim().isEmpty()) {
            String kw = titleKeyword.trim();
            List<Book> books = bookMapper.selectList(new LambdaQueryWrapper<Book>()
                    .eq(Book::getStatus, 1)
                    .like(Book::getTitle, kw));
            if (books.isEmpty()) {
                return new PageResult(0L, new ArrayList<>());
            }
            Set<Long> bookIds = books.stream().map(Book::getId).collect(Collectors.toSet());
            qw.in(SecondHandListing::getBookId, bookIds);
        }
        secondHandListingMapper.selectPage(p, qw);
        List<SecondHandListingVO> vos = toVoList(p.getRecords());
        return new PageResult(p.getTotal(), vos);
    }

    @Override
    public SecondHandListingVO getOnSaleDetail(Long id) {
        if (id == null) {
            throw new BaseException("参数错误");
        }
        SecondHandListing l = secondHandListingMapper.selectById(id);
        if (l == null || l.getStatus() == null || l.getStatus() != SecondHandConstants.STATUS_ON_SALE) {
            throw new BaseException("该二手书不存在或已下架");
        }
        return toVo(l);
    }

    @Override
    public PageResult pageMy(int page, int pageSize, Integer status) {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new BaseException(MessageConstant.LOGIN_FAILED);
        }
        Page<SecondHandListing> p = new Page<>(page, pageSize);
        LambdaQueryWrapper<SecondHandListing> qw = new LambdaQueryWrapper<>();
        qw.eq(SecondHandListing::getSellerUserId, userId)
                .orderByDesc(SecondHandListing::getCreateTime);
        if (status != null) {
            qw.eq(SecondHandListing::getStatus, status);
        }
        secondHandListingMapper.selectPage(p, qw);
        List<SecondHandListingVO> vos = toVoList(p.getRecords());
        return new PageResult(p.getTotal(), vos);
    }

    @Override
    @Transactional
    public void withdraw(Long id) {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new BaseException(MessageConstant.LOGIN_FAILED);
        }
        SecondHandListing l = secondHandListingMapper.selectById(id);
        if (l == null || !userId.equals(l.getSellerUserId())) {
            throw new BaseException("记录不存在");
        }
        if (l.getStatus() == null || l.getStatus() != SecondHandConstants.STATUS_PENDING) {
            throw new BaseException("当前状态不可撤回");
        }
        SecondHandListing upd = SecondHandListing.builder()
                .id(id)
                .status(SecondHandConstants.STATUS_WITHDRAWN)
                .updateTime(LocalDateTime.now())
                .build();
        secondHandListingMapper.updateById(upd);
    }

    @Override
    @Transactional
    public void lockForOrder(Long listingId, String orderId, Long buyerUserId) {
        SecondHandListing l = secondHandListingMapper.selectById(listingId);
        if (l == null || l.getStatus() == null || l.getStatus() != SecondHandConstants.STATUS_ON_SALE) {
            throw new BaseException("该二手书不可购买");
        }
        // 中间商模式：条目审核通过后已视为平台收购，原提交用户也允许购买，不再限制「不能买自己上架」。
        int n = secondHandListingMapper.tryLockForOrder(listingId, orderId);
        if (n != 1) {
            throw new BaseException("手慢一步，该二手书已被抢或下架");
        }
    }

    @Override
    @Transactional
    public void finalizeSoldAfterPaid(String orderId, Long buyerUserId) {
        if (orderId == null || buyerUserId == null) {
            return;
        }
        secondHandListingMapper.finalizeSoldByPendingOrder(orderId, buyerUserId);
        // 中间商模式：卖家（提交用户）已在审核通过时完成平台回收打款，成交后不再二次结算给卖家。
    }

    @Override
    @Transactional
    public void releaseIfOrderCancelled(String orderId) {
        if (orderId == null) {
            return;
        }
        secondHandListingMapper.releaseLockByPendingOrder(orderId);
    }

    @Override
    @Transactional
    public void finalizeSoldForWalletPaidOrder(SecondHandOrder order) {
        if (order == null || order.getListingId() == null) {
            return;
        }
        finalizeSoldAfterPaid(order.getId(), order.getUserId());
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

    private SecondHandListingVO toVo(SecondHandListing l) {
        Book b = bookMapper.selectById(l.getBookId());
        return toVo(l, b);
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

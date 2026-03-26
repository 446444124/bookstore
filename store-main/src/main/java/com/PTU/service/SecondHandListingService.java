package com.PTU.service;

import com.PTU.dto.SecondHandSubmitDTO;
import com.PTU.entity.SecondHandOrder;
import com.PTU.result.PageResult;
import com.PTU.vo.SecondHandListingVO;

public interface SecondHandListingService {

    void submitListing(SecondHandSubmitDTO dto);

    PageResult pageOnSale(int page, int pageSize, String titleKeyword);

    SecondHandListingVO getOnSaleDetail(Long id);

    PageResult pageMy(int page, int pageSize, Integer status);

    void withdraw(Long id);

    /**
     * 下单前锁定条目，orderId 为即将生成的订单号
     */
    void lockForOrder(Long listingId, String orderId, Long buyerUserId);

    void finalizeSoldAfterPaid(String orderId, Long buyerUserId);

    void releaseIfOrderCancelled(String orderId);

    /**
     * 钱包支付成功后，订单已支付，直接完成二手成交
     */
    void finalizeSoldForWalletPaidOrder(SecondHandOrder order);
}

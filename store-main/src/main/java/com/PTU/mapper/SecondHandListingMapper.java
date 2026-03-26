package com.PTU.mapper;

import com.PTU.entity.SecondHandListing;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface SecondHandListingMapper extends BaseMapper<SecondHandListing> {

    @Update("UPDATE second_hand_listing SET status = 8, pending_order_id = #{orderId}, update_time = NOW() " +
            "WHERE id = #{listingId} AND status = 2")
    int tryLockForOrder(@Param("listingId") Long listingId, @Param("orderId") String orderId);

    @Update("UPDATE second_hand_listing SET status = 3, order_id = #{orderId}, buyer_user_id = #{buyerId}, " +
            "pending_order_id = NULL, sold_time = NOW(), update_time = NOW() " +
            "WHERE pending_order_id = #{orderId} AND status = 8")
    int finalizeSoldByPendingOrder(@Param("orderId") String orderId, @Param("buyerId") Long buyerId);

    @Update("UPDATE second_hand_listing SET status = 2, pending_order_id = NULL, update_time = NOW() " +
            "WHERE pending_order_id = #{orderId} AND status = 8")
    int releaseLockByPendingOrder(@Param("orderId") String orderId);
}

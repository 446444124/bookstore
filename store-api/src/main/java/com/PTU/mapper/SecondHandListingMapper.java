package com.PTU.mapper;

import com.PTU.entity.SecondHandListing;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SecondHandListingMapper extends BaseMapper<SecondHandListing> {

    /**
     * 二手书退款通过后重新上架：将已售条目恢复为在售并清空成交信息（幂等）。
     */
    @Update("UPDATE second_hand_listing SET status = 2, buyer_user_id = NULL, order_id = NULL, sold_time = NULL, " +
            "pending_order_id = NULL, update_time = NOW() " +
            "WHERE id = #{listingId} AND status = 3 AND order_id = #{orderId}")
    int relistAfterRefund(@Param("listingId") Long listingId, @Param("orderId") String orderId);
}

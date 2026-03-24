package com.PTU.mapper;

import com.PTU.entity.OrderDetail;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface OrderDetailMapper extends BaseMapper<OrderDetail> {
    void insertBatch(@Param("orderDetailList") List<OrderDetail> orderDetailList);
}

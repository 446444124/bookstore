package com.PTU.mapper;

import com.PTU.vo.AdminOrderUnionRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AdminOrderUnionMapper {

    long countUnion(@Param("status") Integer status,
                    @Param("deliveryWay") Integer deliveryWay,
                    @Param("orderNumber") String orderNumber,
                    @Param("phone") String phone);

    List<AdminOrderUnionRow> selectUnionPage(@Param("status") Integer status,
                                             @Param("deliveryWay") Integer deliveryWay,
                                             @Param("orderNumber") String orderNumber,
                                             @Param("phone") String phone,
                                             @Param("offset") long offset,
                                             @Param("limit") long limit);
}

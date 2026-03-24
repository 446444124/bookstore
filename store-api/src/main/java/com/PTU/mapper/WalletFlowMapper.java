package com.PTU.mapper;

import com.PTU.entity.WalletFlow;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface WalletFlowMapper extends BaseMapper<WalletFlow> {
    @Select("SELECT COUNT(*) FROM wallet_flow WHERE biz_no = #{bizNo} AND flow_type = #{flowType}")
    long countByBizNoAndType(@Param("bizNo") String bizNo, @Param("flowType") Integer flowType);
}


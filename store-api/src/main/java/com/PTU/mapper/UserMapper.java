package com.PTU.mapper;

import com.PTU.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    @Update("UPDATE user SET wallet_balance = IFNULL(wallet_balance, 0) + #{amount} WHERE user_id = #{userId}")
    int addWalletBalance(@Param("userId") Long userId, @Param("amount") BigDecimal amount);
}


package com.PTU.controller;

import com.PTU.constant.JwtClaimsConstant;
import com.PTU.constant.MessageConstant;
import com.PTU.context.BaseContext;
import com.PTU.dto.UserDTO;
import com.PTU.dto.UserLoginDTO;
import com.PTU.entity.User;
import com.PTU.entity.WalletFlow;
import com.PTU.mapper.WalletFlowMapper;
import com.PTU.properties.JwtProperties;
import com.PTU.result.PageResult;
import com.PTU.result.Result;
import com.PTU.service.UserService;
import com.PTU.utils.PayUtil;
import com.PTU.utils.JwtUtil;
import com.PTU.vo.UserLoginVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/user/user")
@Api(tags = "C端用户接口")
@Slf4j
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private JwtProperties jwtProperties;
    @Autowired
    private PayUtil payUtil;
    @Autowired
    private WalletFlowMapper walletFlowMapper;
    @PostMapping("/login")
    @ApiOperation("用户登录")
    public Result<UserLoginVO> login(@RequestBody UserLoginDTO userLoginDTO){
        log.info("用户登录:{}",userLoginDTO);
        //登录
        User user = userService.login(userLoginDTO);
       //  生成jwt令牌
        Map<String,Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID,user.getUserId());
        String token = JwtUtil.createJWT(
                jwtProperties.getUserSecretKey(),
                jwtProperties.getUserTtl(),
                claims);
        UserLoginVO userLoginVO = UserLoginVO.builder()
                .id(user.getUserId())
                .token(token)
                .build();
        return Result.success(userLoginVO);
    }

    @ApiOperation("用户退出")
    @PostMapping("/logout")
    public Result<String> logout() {
        return Result.success();
    }

    @PostMapping("/register")
    @ApiOperation("用户注册")
    public Result register(@RequestBody UserDTO tuser){
        log.info("用户注册:{}",tuser);
        //注册
        return userService.register(tuser);
    }
    @GetMapping("/{id}")
    @ApiOperation("根据id查询用户")
    public Result<User> getById(@PathVariable Long id){
        log.info("根据id查询用户信息：{}", id);
        User user = userService.getById(id);
        // 对密码进行脱敏处理
        user.setPassword("****");
        return Result.success(user);
    }
    @PutMapping
    @ApiOperation("修改用户信息")
    public Result update(@RequestBody User user){
        log.info("修改用户信息：{}", user);
        //仅修改自己的信息
        if(!user.getUserId().equals(BaseContext.getCurrentId())){
            return Result.error("不能修改其他用户的信息");
        }
        if(user.getStudentId()!=null){
            //校验学号合法性(12位纯数字)
            if (!user.getStudentId().matches("^\\d{12}$")) {
                return Result.error(MessageConstant.STUDENT_ID_ERROR);
            }
        }
        //校验手机号合法性(11位纯数字)
        if (user.getPhone() != null && !user.getPhone().matches("^\\d{11}$")) {
            return Result.error("手机号格式错误");
        }

        userService.updateById(user);
        return Result.success();
    }

    @GetMapping("/wallet/balance")
    @ApiOperation("查询钱包余额")
    public Result<BigDecimal> walletBalance() {
        Long userId = BaseContext.getCurrentId();
        return Result.success(userService.getWalletBalance(userId));
    }

    @GetMapping(value = "/wallet/rechargePay", produces = MediaType.TEXT_HTML_VALUE)
    @ApiOperation("钱包支付宝充值")
    public ResponseEntity<String> walletRechargePay(@RequestParam BigDecimal amount) throws Exception {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            return ResponseEntity.ok("<html><body><h3>请先登录后再充值</h3></body></html>");
        }
        if (amount == null || amount.compareTo(new BigDecimal("0.01")) < 0) {
            return ResponseEntity.ok("<html><body><h3>充值金额至少0.01元</h3></body></html>");
        }
        BigDecimal val = amount.setScale(2, BigDecimal.ROUND_HALF_UP);
        String amountCent = val.multiply(new BigDecimal("100")).toBigInteger().toString();
        String outTradeNo = "WALLET_" + userId + "_" + amountCent + "_" + System.currentTimeMillis();
        String html = payUtil.sendRequestToAlipay(outTradeNo, val, "Wallet Recharge");
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(html);
    }

    @GetMapping("/wallet/flows")
    @ApiOperation("查询钱包流水")
    public Result<PageResult> walletFlows(@RequestParam(defaultValue = "1") int page,
                                          @RequestParam(defaultValue = "10") int pageSize) {
        Long userId = BaseContext.getCurrentId();
        Page<WalletFlow> p = new Page<>(page, pageSize);
        LambdaQueryWrapper<WalletFlow> qw = new LambdaQueryWrapper<WalletFlow>()
                .eq(WalletFlow::getUserId, userId)
                .orderByDesc(WalletFlow::getCreateTime);
        walletFlowMapper.selectPage(p, qw);
        return Result.success(new PageResult(p.getTotal(), p.getRecords()));
    }
}

package com.PTU.controller;

import com.PTU.constant.JwtClaimsConstant;
import com.PTU.dto.AdminDTO;
import com.PTU.dto.AdminLoginDTO;
import com.PTU.dto.AdminPageQueryDTO;
import com.PTU.entity.Admin;
import com.PTU.properties.JwtProperties;
import com.PTU.result.PageResult;
import com.PTU.result.Result;
import com.PTU.service.AdminService;
import com.PTU.utils.JwtUtil;
import com.PTU.vo.AdminLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin/admin")
@Api(tags = "B端员工接口")
@Slf4j
public class AdminController {
    @Autowired
    private AdminService adminService;
    @Autowired
    private JwtProperties jwtProperties;

    @PostMapping("/login")
    @ApiOperation("管理员登录")
    public Result<AdminLoginVO> login(@RequestBody AdminLoginDTO adminLoginDTO){
        log.info("管理员登录:{}",adminLoginDTO);
        //登录
        Admin admin = adminService.login(adminLoginDTO);
       //  生成jwt令牌
        Map<String,Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID,admin.getEmployeeId());
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);
        AdminLoginVO adminLoginVO = AdminLoginVO.builder()
                .id(admin.getEmployeeId())
                .token(token)
                .build();
        return Result.success(adminLoginVO);
    }

    @ApiOperation("管理员退出")
    @PostMapping("/logout")
    public Result<String> logout() {
        return Result.success();
    }
    /**
     * 新增员工
     * @param adminDTO
     */
    @ApiOperation("新增员工")
    @PostMapping
    public Result save(@RequestBody AdminDTO adminDTO) {
        log.info("新增员工：{}", adminDTO);
        adminService.save(adminDTO);
        return Result.success() ;
    }
    @ApiOperation("员工分页查询")
    @GetMapping("/page")
    public Result<PageResult> page(AdminPageQueryDTO adminPageQueryDTO){
        log.info("员工分页查询：{}", adminPageQueryDTO);
        PageResult pageResult =adminService.pageQuery(adminPageQueryDTO);
        return Result.success(pageResult);
    }

}

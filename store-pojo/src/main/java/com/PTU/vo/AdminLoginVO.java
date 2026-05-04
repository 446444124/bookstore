package com.PTU.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminLoginVO {
    private Long id;
    private String token;
    /** 岗位（如「店长」），用于前端菜单与路由权限展示 */
    private String position;
}

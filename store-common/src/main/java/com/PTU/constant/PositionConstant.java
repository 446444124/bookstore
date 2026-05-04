package com.PTU.constant;

/**
 * 员工岗位常量（与 employee.position 一致）
 */
public final class PositionConstant {

    private PositionConstant() {
    }

    /** 店长：拥有员工管理等权限 */
    public static final String STORE_MANAGER = "店长";

    public static boolean isStoreManager(String position) {
        return position != null && STORE_MANAGER.equals(position.trim());
    }
}

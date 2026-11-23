package cn.iocoder.yudao.module.mdm.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * MDM 模块错误码（使用 1-050-000-000 段，不与底座/IM 冲突）
 */
public interface ErrorCodeConstants {

    // ========== 设备 (1-050-100-000) ==========
    ErrorCode DEVICE_NOT_EXISTS = new ErrorCode(1_050_100_000, "设备不存在");
    ErrorCode DEVICE_IDENTIFIER_DUPLICATE = new ErrorCode(1_050_100_001, "设备标识已注册");
    ErrorCode DEVICE_STATUS_INVALID = new ErrorCode(1_050_100_002, "设备状态不允许该操作");

    // ========== 指令 (1-050-200-000) ==========
    ErrorCode COMMAND_NOT_EXISTS = new ErrorCode(1_050_200_000, "远程指令不存在");
    ErrorCode COMMAND_CONFIRM_TOKEN_REQUIRED = new ErrorCode(1_050_200_001, "高危指令需要二次确认令牌");
    ErrorCode COMMAND_CONFIRM_TOKEN_MISMATCH = new ErrorCode(1_050_200_002, "二次确认令牌不匹配");
}

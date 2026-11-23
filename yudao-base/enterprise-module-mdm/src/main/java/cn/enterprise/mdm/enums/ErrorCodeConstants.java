package cn.enterprise.mdm.enums;

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

    // ========== 策略 (1-050-300-000) ==========
    ErrorCode POLICY_NOT_EXISTS = new ErrorCode(1_050_300_000, "策略不存在");

    // ========== 分组 (1-050-400-000) ==========
    ErrorCode GROUP_NOT_EXISTS = new ErrorCode(1_050_400_000, "设备分组不存在");
    ErrorCode GROUP_HAS_CHILDREN = new ErrorCode(1_050_400_001, "分组下存在子分组，无法删除");

    // ========== 合规 (1-050-500-000) ==========
    ErrorCode REPORT_NOT_EXISTS = new ErrorCode(1_050_500_000, "合规上报不存在");

    // ========== 应用分发 (1-050-600-000) ==========
    ErrorCode APP_NOT_EXISTS = new ErrorCode(1_050_600_000, "应用不存在");
    ErrorCode APP_PACKAGE_DUPLICATE = new ErrorCode(1_050_600_001, "应用包名已存在");
    ErrorCode APP_VERSION_NOT_EXISTS = new ErrorCode(1_050_600_002, "应用版本不存在");
    ErrorCode APP_OFFLINE_CANNOT_DISTRIBUTE = new ErrorCode(1_050_600_003, "应用已下架，无法分发");
    ErrorCode APP_DISTRIBUTION_NOT_EXISTS = new ErrorCode(1_050_600_004, "分发任务不存在");
    ErrorCode APP_GRAY_PERCENT_INVALID = new ErrorCode(1_050_600_005, "灰度比例必须在 0-100 之间");

    // ========== IM 通知 (1-050-700-000) ==========
    ErrorCode IM_NOTIFICATION_NOT_EXISTS = new ErrorCode(1_050_700_000, "IM 通知记录不存在");

    // ========== 开放平台 (1-050-800-000) ==========
    ErrorCode OPEN_APP_NOT_EXISTS = new ErrorCode(1_050_800_000, "开放平台应用不存在");
    ErrorCode OPEN_APP_KEY_DUPLICATE = new ErrorCode(1_050_800_001, "appKey 已存在");
    ErrorCode WEBHOOK_EVENT_INVALID = new ErrorCode(1_050_800_002, "订阅事件不合法");
    ErrorCode API_QUOTA_EXCEEDED = new ErrorCode(1_050_800_003, "API 调用超出配额限制");
}

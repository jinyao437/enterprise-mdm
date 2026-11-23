package cn.iocoder.yudao.module.mdm.controller.admin.device.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - MDM 设备 Response VO")
@Data
public class DeviceRespVO {

    @Schema(description = "设备编号", example = "1024")
    private Long id;

    @Schema(description = "设备唯一标识", example = "IMEI-123")
    private String identifier;

    @Schema(description = "设备名称", example = "张三的手机")
    private String name;

    @Schema(description = "平台", example = "android")
    private String platform;

    @Schema(description = "系统版本", example = "Android 14")
    private String osVersion;

    @Schema(description = "状态", example = "1")
    private Integer status;

    @Schema(description = "状态名", example = "使用中")
    private String statusName;

    @Schema(description = "绑定用户编号", example = "2048")
    private Long bindUserId;

    @Schema(description = "分组编号", example = "1024")
    private Long groupId;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}

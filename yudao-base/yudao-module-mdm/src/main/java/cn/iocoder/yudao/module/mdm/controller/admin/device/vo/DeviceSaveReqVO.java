package cn.iocoder.yudao.module.mdm.controller.admin.device.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - MDM 设备注册/更新 Request VO")
@Data
public class DeviceSaveReqVO {

    @Schema(description = "设备编号（更新时必填）", example = "1024")
    private Long id;

    @Schema(description = "设备唯一标识", requiredMode = Schema.RequiredMode.REQUIRED, example = "IMEI-123")
    @NotBlank(message = "设备标识不能为空")
    private String identifier;

    @Schema(description = "设备名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三的手机")
    @NotBlank(message = "设备名称不能为空")
    private String name;

    @Schema(description = "平台", requiredMode = Schema.RequiredMode.REQUIRED, example = "android")
    @NotBlank(message = "平台不能为空")
    private String platform;

    @Schema(description = "系统版本", example = "Android 14")
    private String osVersion;

    @Schema(description = "绑定用户编号（芋道系统用户）", example = "2048")
    private Long bindUserId;

    @Schema(description = "分组编号", example = "1024")
    private Long groupId;

    @Schema(description = "状态", example = "0")
    @NotNull(message = "状态不能为空")
    private Integer status;
}

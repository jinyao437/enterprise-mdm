package cn.iocoder.yudao.module.mdm.controller.admin.device.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - MDM 设备分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DevicePageReqVO extends PageParam {

    @Schema(description = "设备标识", example = "IMEI-123")
    private String identifier;

    @Schema(description = "设备名称", example = "张三的手机")
    private String name;

    @Schema(description = "平台", example = "android")
    private String platform;

    @Schema(description = "状态", example = "1")
    private Integer status;

    @Schema(description = "分组编号", example = "1024")
    private Long groupId;
}

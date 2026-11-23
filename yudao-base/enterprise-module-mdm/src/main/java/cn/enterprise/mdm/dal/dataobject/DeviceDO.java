package cn.enterprise.mdm.dal.dataobject;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * MDM 设备 DO。
 *
 * <p>继承 {@link TenantBaseDO}：tenantId 字段由基类提供，租户隔离由芋道底座的
 * MyBatis-Plus 租户插件自动注入 WHERE 条件——本模块不重复实现租户隔离。
 */
@TableName("mdm_device")
@KeySequence("mdm_device_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceDO extends TenantBaseDO {

    @TableId
    private Long id;

    /** 设备唯一标识（IMEI / SN / UUID） */
    private String identifier;

    /** 设备名称 */
    private String name;

    /** 平台：android / ios / harmony / windows / macos */
    private String platform;

    /** 系统版本 */
    private String osVersion;

    /** 状态：{@link cn.enterprise.mdm.enums.DeviceStatusEnum} */
    private Integer status;

    /** 绑定用户编号（复用芋道系统用户，不自建人员体系） */
    private Long bindUserId;

    /** 设备分组编号 */
    private Long groupId;
}

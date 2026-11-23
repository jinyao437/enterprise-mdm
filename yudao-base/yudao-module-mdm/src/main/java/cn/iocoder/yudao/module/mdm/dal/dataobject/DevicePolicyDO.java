package cn.iocoder.yudao.module.mdm.dal.dataobject;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 设备策略 DO（继承 TenantBaseDO，租户隔离由底座插件自动处理）。
 */
@TableName("mdm_device_policy")
@KeySequence("mdm_device_policy_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DevicePolicyDO extends TenantBaseDO {

    @TableId
    private Long id;

    /** 策略名称 */
    private String name;

    /** 策略类型：{@link cn.iocoder.yudao.module.mdm.enums.PolicyTypeEnum} */
    private Integer type;

    /** 策略内容（JSON 配置） */
    private String content;

    /** 生效范围：GROUP(按设备分组) / DEVICE(指定设备) */
    private String scopeType;

    /** 生效目标编号（分组id 或 设备id） */
    private Long scopeId;

    /** 优先级，越大越高 */
    private Integer priority;

    /** 是否启用 */
    private Boolean enabled;
}

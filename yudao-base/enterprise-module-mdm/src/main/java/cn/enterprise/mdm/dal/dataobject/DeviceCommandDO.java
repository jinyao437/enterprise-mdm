package cn.enterprise.mdm.dal.dataobject;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 远程管控指令 DO（继承 TenantBaseDO，租户隔离由底座插件自动处理）。
 *
 * <p>confirmToken 为预发放的二次确认令牌（随机串），绑定高危指令请求，
 * 不使用数据库自增 id 作为校验锚点——规避评审 M-2 的锚点错误。
 */
@TableName("mdm_device_command")
@KeySequence("mdm_device_command_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceCommandDO extends TenantBaseDO {

    @TableId
    private Long id;

    /** 目标设备编号 */
    private Long deviceId;

    /** 指令类型：{@link cn.enterprise.mdm.enums.CommandTypeEnum} */
    private Integer type;

    /** 指令参数（JSON） */
    private String params;

    /** 状态：{@link cn.enterprise.mdm.enums.CommandStatusEnum} */
    private Integer status;

    /** 幂等键（含租户维度，规避评审 M-1） */
    private String idempotencyKey;

    /** 高危指令预发放的二次确认令牌（仅 WIPE 等使用） */
    private String confirmToken;

    /** 已下发次数（失败退避重试） */
    private Integer dispatchAttempts;
}

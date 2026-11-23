package cn.enterprise.mdm.dal.dataobject;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * MDM 设备合规上报 DO（继承 TenantBaseDO 复用底座租户隔离）。
 *
 * <p>设备 agent 定期上报合规状态；不合规触发告警，形成"检查-告警-处理"闭环。
 */
@TableName("mdm_compliance_report")
@KeySequence("mdm_compliance_report_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComplianceReportDO extends TenantBaseDO {

    @TableId
    private Long id;

    /** 设备编号 */
    private Long deviceId;

    /** 检查项（如 password_strength / jailbreak / encryption） */
    private String checkItem;

    /** 合规状态：{@link cn.enterprise.mdm.enums.ComplianceStatusEnum} */
    private Integer status;

    /** 检查详情（JSON） */
    private String detail;

    /** 是否已告警 */
    private Boolean alerted;

    /** 上报时间 */
    private LocalDateTime reportTime;
}

package cn.enterprise.mdm.dal.dataobject;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 应用分发任务 DO（继承 TenantBaseDO 复用底座租户隔离）。
 *
 * <p>支持按分组/设备分发与灰度比例；灰度通过 grayPercent 控制目标设备命中范围。
 */
@TableName("mdm_app_distribution")
@KeySequence("mdm_app_distribution_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppDistributionDO extends TenantBaseDO {

    @TableId
    private Long id;

    /** 应用编号 */
    private Long appId;

    /** 版本编号 */
    private Long versionId;

    /** 分发范围：ALL 全量 / GROUP 分组 / DEVICE 指定设备 */
    private String scopeType;

    /** 范围目标编号（分组id/设备id，ALL 时为 0） */
    private Long scopeId;

    /** 灰度比例 0-100（100 为全量） */
    private Integer grayPercent;

    /** 状态：{@link cn.enterprise.mdm.enums.DistributionStatusEnum} */
    private Integer status;

    /** 已成功下发数量 */
    private Integer successCount;

    /** 已失败数量 */
    private Integer failCount;
}

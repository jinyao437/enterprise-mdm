package cn.enterprise.mdm.dal.dataobject;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 开放平台 API 配额 DO（继承 TenantBaseDO 复用底座租户隔离）。
 *
 * <p>租户级配额与限流：按应用+接口维度限制单位时间调用次数。
 */
@TableName("mdm_api_quota")
@KeySequence("mdm_api_quota_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiQuotaDO extends TenantBaseDO {

    @TableId
    private Long id;

    /** 应用编号 */
    private Long appId;

    /** 接口标识（如 device:list），* 表示全部 */
    private String apiPattern;

    /** 每秒允许调用次数 */
    private Integer qps;

    /** 每日调用上限 */
    private Long dailyLimit;

    /** 是否启用 */
    private Boolean enabled;
}

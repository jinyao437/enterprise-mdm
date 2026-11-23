package cn.enterprise.mdm.dal.dataobject;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 企业应用 DO（继承 TenantBaseDO 复用底座租户隔离）。
 */
@TableName("mdm_app")
@KeySequence("mdm_app_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppDO extends TenantBaseDO {

    @TableId
    private Long id;

    /** 应用名称 */
    private String name;

    /** 应用包名（android package / iOS bundleId） */
    private String packageName;

    /** 应用类型：INTERNAL 企业自建 / THIRD_PARTY 三方接入 */
    private String appType;

    /** 状态：{@link cn.enterprise.mdm.enums.AppStatusEnum} */
    private Integer status;

    /** 应用图标 URL */
    private String icon;

    /** 应用描述 */
    private String description;

    /** 三方应用来源（开放平台 appId，THIRD_PARTY 时有值） */
    private String openAppId;
}

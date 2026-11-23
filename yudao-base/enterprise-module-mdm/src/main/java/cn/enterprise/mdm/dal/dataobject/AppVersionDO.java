package cn.enterprise.mdm.dal.dataobject;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 应用版本 DO（继承 TenantBaseDO 复用底座租户隔离）。
 */
@TableName("mdm_app_version")
@KeySequence("mdm_app_version_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppVersionDO extends TenantBaseDO {

    @TableId
    private Long id;

    /** 所属应用编号 */
    private Long appId;

    /** 版本号（展示用，如 1.2.0） */
    private String version;

    /** 版本码（用于客户端比较大小） */
    private Long versionCode;

    /** 安装包下载地址 */
    private String packageUrl;

    /** 安装包校验值（SHA-256，防篡改） */
    private String checksum;

    /** 更新说明 */
    private String changelog;

    /** 是否强制更新 */
    private Boolean forceUpdate;
}

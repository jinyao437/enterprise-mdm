package cn.enterprise.mdm.dal.dataobject;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 设备访问令牌 DO（设备端专用 API 鉴权用，继承 TenantBaseDO 复用租户隔离）。
 *
 * <p>管理员在 Web 后台为设备生成激活令牌，agent 激活后用它调用设备端 API。
 * tokenHash 存哈希不存明文（NFR-SEC-01）；令牌可撤销/过期。
 */
@TableName("mdm_device_token")
@KeySequence("mdm_device_token_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceTokenDO extends TenantBaseDO {

    @TableId
    private Long id;

    /** 令牌哈希（SHA-256，明文只在创建时返回一次） */
    private String tokenHash;

    /** 绑定的设备编号 */
    private Long deviceId;

    /** 设备标识（IMEI/SN，便于检索） */
    private String identifier;

    /** 状态：1启用 2撤销 */
    private Integer status;

    /** 过期时间 */
    private LocalDateTime expireTime;

    /** 最近使用时间 */
    private LocalDateTime lastUsedTime;
}

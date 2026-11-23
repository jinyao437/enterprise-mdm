package cn.enterprise.mdm.dal.dataobject;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Webhook 事件订阅 DO（继承 TenantBaseDO 复用底座租户隔离）。
 *
 * <p>secretEnc 存加密后的签名密钥（生产接 KMS；不存明文，NFR-SEC-01）。
 */
@TableName("mdm_webhook_subscription")
@KeySequence("mdm_webhook_subscription_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookSubscriptionDO extends TenantBaseDO {

    @TableId
    private Long id;

    /** 应用编号 */
    private Long appId;

    /** 订阅事件：{@link cn.enterprise.mdm.enums.WebhookEventEnum} */
    private String event;

    /** 回调地址 */
    private String url;

    /** 签名密钥密文（生产接 KMS，不存明文） */
    private String secretEnc;

    /** 是否启用 */
    private Boolean enabled;

    /** 最近推送时间 */
    private LocalDateTime lastPushTime;
}

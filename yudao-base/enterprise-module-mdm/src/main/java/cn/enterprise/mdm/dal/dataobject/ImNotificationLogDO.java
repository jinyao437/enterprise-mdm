package cn.enterprise.mdm.dal.dataobject;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * IM 业务通知日志（本地消息表，保证业务联动消息必达）。
 *
 * <p>继承 TenantBaseDO 复用底座租户隔离；幂等键含 tenant_id（规避评审 M-1：
 * SHARED 档跨租户 eventId 重叠丢通知）。
 */
@TableName("mdm_im_notification_log")
@KeySequence("mdm_im_notification_log_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImNotificationLogDO extends TenantBaseDO {

    @TableId
    private Long id;

    /** 幂等键 = tenantId:eventId:recipient（含租户维度） */
    private String idempotencyKey;

    /** 业务事件编号 */
    private String eventId;

    /** 消息类型：{@link cn.enterprise.mdm.enums.ImMessageTypeEnum} */
    private Integer messageType;

    /** 接收人（平台用户编号） */
    private Long recipient;

    /** 消息标题 */
    private String title;

    /** 消息内容 */
    private String content;

    /** 发送状态：{@link cn.enterprise.mdm.enums.ImNotifyStatusEnum} */
    private Integer status;

    /** 发送重试次数 */
    private Integer retryCount;

    /** IM 侧消息 id（发送成功后回填） */
    private String imMessageId;
}

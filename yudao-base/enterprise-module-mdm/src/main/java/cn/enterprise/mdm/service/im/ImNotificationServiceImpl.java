package cn.enterprise.mdm.service.im;

import cn.enterprise.mdm.client.ImClient;
import cn.enterprise.mdm.dal.dataobject.ImNotificationLogDO;
import cn.enterprise.mdm.dal.mysql.ImNotificationLogMapper;
import cn.enterprise.mdm.enums.ImNotifyStatusEnum;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;

/**
 * IM 业务通知 Service 实现。
 *
 * <p>租户上下文复用底座 {@link TenantContextHolder}（缺上下文由底座 fail-closed）；
 * 幂等键拼 tenantId 前缀（M-1 正确做法），DB 唯一键 (tenant_id, idempotency_key)。
 */
@Service
@Validated
public class ImNotificationServiceImpl implements ImNotificationService {

    @Resource
    private ImNotificationLogMapper logMapper;
    @Resource
    private ImClient imClient;

    @Override
    public Long enqueue(String eventId, Integer messageType, Long recipient, String title, String content) {
        Long tenantId = TenantContextHolder.getRequiredTenantId();
        String idempotencyKey = tenantId + ":" + eventId + ":" + recipient;

        // 幂等命中：直接返回已有记录，避免重复通知
        ImNotificationLogDO exists = logMapper.selectByIdempotencyKey(idempotencyKey);
        if (exists != null) {
            return exists.getId();
        }

        ImNotificationLogDO log = ImNotificationLogDO.builder()
                .idempotencyKey(idempotencyKey)
                .eventId(eventId)
                .messageType(messageType)
                .recipient(recipient)
                .title(title)
                .content(content)
                .status(ImNotifyStatusEnum.PENDING.getStatus())
                .retryCount(0)
                .build();
        logMapper.insert(log);
        return log.getId();
    }

    @Override
    public List<ImNotificationLogDO> listPending() {
        return logMapper.selectPending();
    }

    @Override
    public boolean trySend(Long notificationId) {
        ImNotificationLogDO log = logMapper.selectById(notificationId);
        if (log == null) {
            return false;
        }
        try {
            String imMessageId = imClient.sendPrivateMessage(
                    log.getTenantId(), log.getRecipient(), log.getTitle(), log.getContent());
            ImNotificationLogDO update = new ImNotificationLogDO();
            update.setId(notificationId);
            update.setStatus(ImNotifyStatusEnum.SENT.getStatus());
            update.setImMessageId(imMessageId);
            logMapper.updateById(update);
            return true;
        } catch (Exception ex) {
            // 失败不吞掉：置 FAILED 并累计重试次数，由定时任务继续扫描（必达语义）
            ImNotificationLogDO update = new ImNotificationLogDO();
            update.setId(notificationId);
            update.setStatus(ImNotifyStatusEnum.FAILED.getStatus());
            update.setRetryCount((log.getRetryCount() == null ? 0 : log.getRetryCount()) + 1);
            logMapper.updateById(update);
            return false;
        }
    }
}

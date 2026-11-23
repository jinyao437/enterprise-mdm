package cn.enterprise.mdm.service.im;

import cn.enterprise.mdm.dal.dataobject.ImNotificationLogDO;

import java.util.List;

/**
 * IM 业务通知服务（审批/告警/策略变更经 IM 通知，本地消息表保必达）
 */
public interface ImNotificationService {

    /**
     * 提交一条业务通知（幂等：同租户同 eventId 同接收人不重复）。
     *
     * @param eventId     业务事件编号（建议全局唯一）
     * @param messageType 消息类型
     * @param recipient   接收人（平台用户编号）
     * @param title       标题
     * @param content     内容
     * @return 通知日志编号
     */
    Long enqueue(String eventId, Integer messageType, Long recipient, String title, String content);

    /** 扫描待发送通知 */
    List<ImNotificationLogDO> listPending();

    /**
     * 尝试发送一条通知（供定时任务调用）。
     * 成功回填 imMessageId 并置 SENT；失败置 FAILED 且 retryCount+1。
     *
     * @return 是否发送成功
     */
    boolean trySend(Long notificationId);

    /**
     * 定时任务入口：跨租户扫描所有待发通知并逐条发送。
     *
     * <p>无 Web 请求也可能被调用，因此内部自行处理租户上下文：
     * 先用忽略租户查询拿到全部待发记录，再按每条记录的 tenantId 用
     * TenantUtils.execute 包裹发送，避免租户插件在无上下文时失败。
     *
     * @return 本次成功发送条数
     */
    int scanAndSendAllTenants();
}

package cn.enterprise.mdm.client;

/**
 * IM 客户端适配接口。
 *
 * <p>业务侧只依赖本接口，不依赖具体 IM 实现——野火开源版与后期采购的商业版
 * 各自实现本接口即可切换，业务代码不动（对应设计决策：商业可插拔）。
 *
 * <p><b>注意：</b>芋道底座自带的 {@code yudao-module-im} 是底座自研 IM 的消息/群/好友能力，
 * 本接口是 MDM 业务「审批/告警/策略变更经 IM 通知」的对接适配，不重复底座 IM 实现。
 */
public interface ImClient {

    /**
     * 发送单聊消息。
     *
     * @param tenantId 租户编号（隔离维度）
     * @param userId   接收人（平台用户，适配层映射到 IM 账号）
     * @param title    消息标题
     * @param content  消息内容
     * @return IM 侧消息 id
     */
    String sendPrivateMessage(Long tenantId, Long userId, String title, String content);

    /**
     * 发送群消息（群由业务定义，如设备告警群）。
     */
    String sendGroupMessage(Long tenantId, String groupCode, String title, String content);

    /**
     * 通讯录单向同步一个用户（平台组织是 IM 通讯录源）。
     */
    boolean syncUser(Long tenantId, Long userId, String imUserId, String displayName);
}

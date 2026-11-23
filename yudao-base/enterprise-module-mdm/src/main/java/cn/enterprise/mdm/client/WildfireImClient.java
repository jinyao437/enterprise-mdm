package cn.enterprise.mdm.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 野火 IM（WildfireIM）适配实现骨架。
 *
 * <p>真实接入需要野火服务端地址与管理员密钥（开源版自建或商业版）。
 * 本类把「未配置」作为硬失败处理：直接抛异常，绝不伪造发送成功或静默吞掉。
 * 接入时在此处补上与野火服务端 REST/SDK 的实际调用即可，业务侧无需改动（依赖 ImClient 接口）。
 */
@Component
public class WildfireImClient implements ImClient {

    /** 野火服务端地址，配置形如 http://im.example.com:8080 */
    @Value("${mdm.im.wildfire.server-url:}")
    private String serverUrl;

    /** 野火管理员密钥（从配置中心注入，禁止硬编码） */
    @Value("${mdm.im.wildfire.admin-secret:}")
    private String adminSecret;

    @Override
    public String sendPrivateMessage(Long tenantId, Long userId, String title, String content) {
        ensureConfigured();
        ensureArgs(tenantId, userId, title, content);
        // TODO 真实接入：调用野火 IM 服务端 API 发送单聊消息，返回其 messageId
        throw new UnsupportedOperationException("野火 IM 服务端 REST 调用尚未接入（需配置 mdm.im.wildfire.* 并实现发送逻辑）");
    }

    @Override
    public String sendGroupMessage(Long tenantId, String groupCode, String title, String content) {
        ensureConfigured();
        if (tenantId == null || groupCode == null || groupCode.isEmpty()) {
            throw new IllegalArgumentException("租户编号与群 code 不能为空");
        }
        // TODO 真实接入：调用野火 IM 服务端 API 发送群消息
        throw new UnsupportedOperationException("野火 IM 服务端 REST 调用尚未接入（需配置 mdm.im.wildfire.* 并实现发送逻辑）");
    }

    @Override
    public boolean syncUser(Long tenantId, Long userId, String imUserId, String displayName) {
        ensureConfigured();
        if (tenantId == null || userId == null || imUserId == null || imUserId.isEmpty()) {
            throw new IllegalArgumentException("租户编号/用户编号/IM 账号不能为空");
        }
        // TODO 真实接入：调用野火 IM 服务端 API 创建/更新用户与通讯录
        throw new UnsupportedOperationException("野火 IM 通讯录同步尚未接入（需配置 mdm.im.wildfire.* 并实现同步逻辑）");
    }

    /** 未配置服务端时拒绝工作（fail-closed），不伪造成功 */
    private void ensureConfigured() {
        if (serverUrl == null || serverUrl.isEmpty() || adminSecret == null || adminSecret.isEmpty()) {
            throw new IllegalStateException("野火 IM 未配置（mdm.im.wildfire.server-url / admin-secret），拒绝发送。请先完成开源版自建或商业版接入。");
        }
    }

    private void ensureArgs(Long tenantId, Long userId, String title, String content) {
        if (tenantId == null || userId == null) {
            throw new IllegalArgumentException("租户编号与用户编号不能为空");
        }
        if (title == null || title.isEmpty() || content == null || content.isEmpty()) {
            throw new IllegalArgumentException("消息标题与内容不能为空");
        }
    }
}

package cn.enterprise.mdm.service.openapi;

import cn.enterprise.mdm.dal.dataobject.ApiQuotaDO;
import cn.enterprise.mdm.dal.dataobject.OpenAppDO;
import cn.enterprise.mdm.dal.dataobject.WebhookSubscriptionDO;

import java.util.List;

/**
 * 开放平台 Service（三方应用注册 + 配额 + Webhook 订阅）。
 *
 * <p>鉴权复用芋道底座 OAuth2（clientId/secret/client_credentials），
 * 本服务只做底座没有的租户级登记、配额、事件订阅。
 */
public interface OpenPlatformService {

    // ===== 应用 =====
    Long registerApp(OpenAppDO app);

    void updateAppStatus(Long id, Integer status);

    OpenAppDO validateAppExists(Long id);

    OpenAppDO getAppByAppKey(String appKey);

    // ===== 配额 =====
    Long setQuota(ApiQuotaDO quota);

    List<ApiQuotaDO> listQuotas(Long appId);

    /** 校验某应用调用某接口是否超出配额（限流判定，纯逻辑可测） */
    boolean checkQuota(Integer currentQps, Long dailyUsed, ApiQuotaDO quota);

    // ===== Webhook =====
    Long subscribe(WebhookSubscriptionDO subscription);

    List<WebhookSubscriptionDO> listSubscribers(String event);
}

package cn.enterprise.mdm.service.openapi;

import cn.enterprise.mdm.dal.dataobject.ApiQuotaDO;
import cn.enterprise.mdm.dal.dataobject.OpenAppDO;
import cn.enterprise.mdm.dal.dataobject.WebhookSubscriptionDO;
import cn.enterprise.mdm.dal.mysql.ApiQuotaMapper;
import cn.enterprise.mdm.dal.mysql.OpenAppMapper;
import cn.enterprise.mdm.dal.mysql.WebhookSubscriptionMapper;
import cn.enterprise.mdm.enums.WebhookEventEnum;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.enterprise.mdm.enums.ErrorCodeConstants.*;

/**
 * 开放平台 Service 实现（租户隔离由底座 TenantBaseDO + 插件处理，本类无租户代码）。
 */
@Service
@Validated
public class OpenPlatformServiceImpl implements OpenPlatformService {

    @Resource
    private OpenAppMapper appMapper;
    @Resource
    private ApiQuotaMapper quotaMapper;
    @Resource
    private WebhookSubscriptionMapper subscriptionMapper;

    // ===== 应用 =====
    @Override
    public Long registerApp(OpenAppDO app) {
        if (appMapper.selectByAppKey(app.getAppKey()) != null) {
            throw exception(OPEN_APP_KEY_DUPLICATE);
        }
        app.setId(null);
        if (app.getStatus() == null) {
            app.setStatus(1);
        }
        appMapper.insert(app);
        return app.getId();
    }

    @Override
    public void updateAppStatus(Long id, Integer status) {
        validateAppExists(id);
        OpenAppDO update = new OpenAppDO();
        update.setId(id);
        update.setStatus(status);
        appMapper.updateById(update);
    }

    @Override
    public OpenAppDO validateAppExists(Long id) {
        OpenAppDO app = appMapper.selectById(id);
        if (app == null) {
            throw exception(OPEN_APP_NOT_EXISTS);
        }
        return app;
    }

    @Override
    public OpenAppDO getAppByAppKey(String appKey) {
        return appMapper.selectByAppKey(appKey);
    }

    // ===== 配额 =====
    @Override
    public Long setQuota(ApiQuotaDO quota) {
        validateAppExists(quota.getAppId());
        quota.setId(null);
        if (quota.getEnabled() == null) {
            quota.setEnabled(true);
        }
        quotaMapper.insert(quota);
        return quota.getId();
    }

    @Override
    public List<ApiQuotaDO> listQuotas(Long appId) {
        return quotaMapper.selectEnabledByApp(appId);
    }

    @Override
    public boolean checkQuota(Integer currentQps, Long dailyUsed, ApiQuotaDO quota) {
        if (quota == null || !Boolean.TRUE.equals(quota.getEnabled())) {
            return true; // 无配额配置/未启用 => 不限
        }
        if (quota.getQps() != null && currentQps != null && currentQps > quota.getQps()) {
            return false;
        }
        if (quota.getDailyLimit() != null && dailyUsed != null && dailyUsed > quota.getDailyLimit()) {
            return false;
        }
        return true;
    }

    // ===== Webhook =====
    @Override
    public Long subscribe(WebhookSubscriptionDO subscription) {
        validateAppExists(subscription.getAppId());
        if (!WebhookEventEnum.isValid(subscription.getEvent())) {
            throw exception(WEBHOOK_EVENT_INVALID);
        }
        subscription.setId(null);
        if (subscription.getEnabled() == null) {
            subscription.setEnabled(true);
        }
        subscriptionMapper.insert(subscription);
        return subscription.getId();
    }

    @Override
    public List<WebhookSubscriptionDO> listSubscribers(String event) {
        return subscriptionMapper.selectByEvent(event);
    }
}

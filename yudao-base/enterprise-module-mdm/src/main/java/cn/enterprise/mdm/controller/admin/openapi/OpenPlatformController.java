package cn.enterprise.mdm.controller.admin.openapi;

import cn.enterprise.mdm.dal.dataobject.ApiQuotaDO;
import cn.enterprise.mdm.dal.dataobject.OpenAppDO;
import cn.enterprise.mdm.dal.dataobject.WebhookSubscriptionDO;
import cn.enterprise.mdm.service.openapi.OpenPlatformService;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 管理后台 - MDM 开放平台
 *
 * <p>鉴权走底座 OAuth2（/system/oauth2/token），本 Controller 只做应用/配额/订阅管理。
 */
@Tag(name = "管理后台 - MDM 开放平台")
@RestController
@RequestMapping("/mdm/open")
@Validated
public class OpenPlatformController {

    @Resource
    private OpenPlatformService openPlatformService;

    @PostMapping("/app/register")
    @Operation(summary = "注册三方应用（appKey 对应底座 OAuth2 clientId）")
    @PreAuthorize("@ss.hasPermission('mdm:open:app')")
    public CommonResult<Long> registerApp(@RequestBody OpenAppDO app) {
        return success(openPlatformService.registerApp(app));
    }

    @PutMapping("/app/status")
    @Operation(summary = "启用/停用应用")
    @PreAuthorize("@ss.hasPermission('mdm:open:app')")
    public CommonResult<Boolean> updateAppStatus(@RequestParam("id") Long id,
                                                 @RequestParam("status") Integer status) {
        openPlatformService.updateAppStatus(id, status);
        return success(true);
    }

    @PostMapping("/quota")
    @Operation(summary = "设置 API 配额")
    @PreAuthorize("@ss.hasPermission('mdm:open:quota')")
    public CommonResult<Long> setQuota(@RequestBody ApiQuotaDO quota) {
        return success(openPlatformService.setQuota(quota));
    }

    @GetMapping("/quota/list")
    @Operation(summary = "应用配额列表")
    @PreAuthorize("@ss.hasPermission('mdm:open:quota')")
    public CommonResult<List<ApiQuotaDO>> listQuotas(@RequestParam("appId") Long appId) {
        return success(openPlatformService.listQuotas(appId));
    }

    @PostMapping("/webhook/subscribe")
    @Operation(summary = "订阅 Webhook 事件")
    @PreAuthorize("@ss.hasPermission('mdm:open:webhook')")
    public CommonResult<Long> subscribe(@RequestBody WebhookSubscriptionDO subscription) {
        return success(openPlatformService.subscribe(subscription));
    }

    @GetMapping("/webhook/subscribers")
    @Operation(summary = "查询某事件的订阅者（供事件分发）")
    @PreAuthorize("@ss.hasPermission('mdm:open:webhook')")
    public CommonResult<List<WebhookSubscriptionDO>> listSubscribers(@RequestParam("event") String event) {
        return success(openPlatformService.listSubscribers(event));
    }
}

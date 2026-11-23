package cn.enterprise.mdm.controller.admin.app;

import cn.enterprise.mdm.dal.dataobject.AppDistributionDO;
import cn.enterprise.mdm.dal.dataobject.AppDO;
import cn.enterprise.mdm.dal.dataobject.AppVersionDO;
import cn.enterprise.mdm.service.app.AppService;
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
 * 管理后台 - MDM 应用分发
 */
@Tag(name = "管理后台 - MDM 应用分发")
@RestController
@RequestMapping("/mdm/app")
@Validated
public class AppController {

    @Resource
    private AppService appService;

    @PostMapping("/create")
    @Operation(summary = "创建应用")
    @PreAuthorize("@ss.hasPermission('mdm:app:create')")
    public CommonResult<Long> createApp(@RequestBody AppDO app) {
        return success(appService.createApp(app));
    }

    @PutMapping("/update")
    @Operation(summary = "更新应用")
    @PreAuthorize("@ss.hasPermission('mdm:app:update')")
    public CommonResult<Boolean> updateApp(@RequestBody AppDO app) {
        appService.updateApp(app);
        return success(true);
    }

    @PutMapping("/status")
    @Operation(summary = "上架/下架")
    @PreAuthorize("@ss.hasPermission('mdm:app:update')")
    public CommonResult<Boolean> updateStatus(@RequestParam("id") Long id,
                                              @RequestParam("status") Integer status) {
        appService.updateAppStatus(id, status);
        return success(true);
    }

    @GetMapping("/list")
    @Operation(summary = "应用列表")
    @PreAuthorize("@ss.hasPermission('mdm:app:query')")
    public CommonResult<List<AppDO>> getAppList() {
        return success(appService.getAppList());
    }

    @PostMapping("/version/add")
    @Operation(summary = "添加应用版本")
    @PreAuthorize("@ss.hasPermission('mdm:app:update')")
    public CommonResult<Long> addVersion(@RequestBody AppVersionDO version) {
        return success(appService.addVersion(version));
    }

    @GetMapping("/version/list")
    @Operation(summary = "应用版本列表")
    @PreAuthorize("@ss.hasPermission('mdm:app:query')")
    public CommonResult<List<AppVersionDO>> getVersionList(@RequestParam("appId") Long appId) {
        return success(appService.getVersionList(appId));
    }

    @PostMapping("/distribution/create")
    @Operation(summary = "创建分发任务（含灰度）")
    @PreAuthorize("@ss.hasPermission('mdm:app:distribute')")
    public CommonResult<Long> createDistribution(@RequestBody AppDistributionDO distribution) {
        return success(appService.createDistribution(distribution));
    }

    @PutMapping("/distribution/pause")
    @Operation(summary = "暂停分发")
    @PreAuthorize("@ss.hasPermission('mdm:app:distribute')")
    public CommonResult<Boolean> pauseDistribution(@RequestParam("id") Long id) {
        appService.pauseDistribution(id);
        return success(true);
    }
}

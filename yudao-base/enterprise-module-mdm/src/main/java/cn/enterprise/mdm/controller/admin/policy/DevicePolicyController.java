package cn.enterprise.mdm.controller.admin.policy;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.enterprise.mdm.dal.dataobject.DevicePolicyDO;
import cn.enterprise.mdm.service.policy.DevicePolicyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 管理后台 - MDM 设备策略
 */
@Tag(name = "管理后台 - MDM 设备策略")
@RestController
@RequestMapping("/mdm/policy")
@Validated
public class DevicePolicyController {

    @Resource
    private DevicePolicyService policyService;

    @PostMapping("/create")
    @Operation(summary = "创建策略")
    @PreAuthorize("@ss.hasPermission('mdm:policy:create')")
    public CommonResult<Long> createPolicy(@RequestBody DevicePolicyDO policy) {
        return success(policyService.createPolicy(policy));
    }

    @PutMapping("/update")
    @Operation(summary = "更新策略")
    @PreAuthorize("@ss.hasPermission('mdm:policy:update')")
    public CommonResult<Boolean> updatePolicy(@RequestBody DevicePolicyDO policy) {
        policyService.updatePolicy(policy);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除策略")
    @PreAuthorize("@ss.hasPermission('mdm:policy:delete')")
    public CommonResult<Boolean> deletePolicy(@RequestParam("id") Long id) {
        policyService.deletePolicy(id);
        return success(true);
    }

    @GetMapping("/effective")
    @Operation(summary = "查询某范围的生效策略（按优先级倒序）")
    @PreAuthorize("@ss.hasPermission('mdm:policy:query')")
    public CommonResult<List<DevicePolicyDO>> getEffectivePolicies(
            @RequestParam("scopeType") String scopeType,
            @RequestParam("scopeId") Long scopeId) {
        return success(policyService.getEffectivePolicies(scopeType, scopeId));
    }
}

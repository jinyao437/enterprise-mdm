package cn.enterprise.mdm.controller.admin.compliance;

import cn.enterprise.mdm.dal.dataobject.ComplianceReportDO;
import cn.enterprise.mdm.service.compliance.ComplianceService;
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
 * 管理后台 - MDM 合规检查
 */
@Tag(name = "管理后台 - MDM 合规检查")
@RestController
@RequestMapping("/mdm/compliance")
@Validated
public class ComplianceController {

    @Resource
    private ComplianceService complianceService;

    @PostMapping("/report")
    @Operation(summary = "设备 agent 上报合规状态（不合规自动进入待告警）")
    public CommonResult<Long> report(@RequestBody ComplianceReportDO report) {
        return success(complianceService.report(report));
    }

    @GetMapping("/unalerted")
    @Operation(summary = "查询未告警的不合规上报（供告警任务扫描）")
    @PreAuthorize("@ss.hasPermission('mdm:compliance:query')")
    public CommonResult<List<ComplianceReportDO>> listUnalerted() {
        return success(complianceService.listUnalertedNonCompliant());
    }

    @PutMapping("/alerted")
    @Operation(summary = "标记已告警")
    @PreAuthorize("@ss.hasPermission('mdm:compliance:update')")
    public CommonResult<Boolean> markAlerted(@RequestParam("id") Long id) {
        complianceService.markAlerted(id);
        return success(true);
    }
}

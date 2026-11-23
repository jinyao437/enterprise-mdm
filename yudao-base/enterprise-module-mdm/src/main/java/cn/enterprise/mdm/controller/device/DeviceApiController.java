package cn.enterprise.mdm.controller.device;

import cn.enterprise.mdm.dal.dataobject.ComplianceReportDO;
import cn.enterprise.mdm.dal.dataobject.DeviceDO;
import cn.enterprise.mdm.dal.dataobject.DevicePolicyDO;
import cn.enterprise.mdm.service.compliance.ComplianceService;
import cn.enterprise.mdm.service.device.DeviceService;
import cn.enterprise.mdm.service.policy.DevicePolicyService;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 设备端专用 API（agent 调用）。
 *
 * <p>与管理端 Controller 分离：
 * - 管理端：/mdm/device/**，需管理员登录 + @PreAuthorize
 * - 设备端：/mdm/device-api/**，@PermitAll 免管理端登录，改用 X-Device-Token 鉴权（DeviceTokenFilter）
 *
 * <p>租户由令牌推导（Filter 注入上下文），deviceId 由 Filter 放入 request attribute，
 * 不信任客户端传参，防越权。
 */
@Tag(name = "设备端 API - MDM Agent")
@RestController
@RequestMapping("/mdm/device-api")
@Validated
@PermitAll
public class DeviceApiController {

    @Resource
    private DeviceService deviceService;
    @Resource
    private DevicePolicyService policyService;
    @Resource
    private ComplianceService complianceService;

    @GetMapping("/enroll")
    @Operation(summary = "设备激活信息（校验令牌并取回设备信息）")
    public CommonResult<DeviceDO> enroll(@RequestAttribute("mdmDeviceId") Long deviceId) {
        return success(deviceService.validateDeviceExists(deviceId));
    }

    @PostMapping("/heartbeat")
    @Operation(summary = "设备心跳（上报合规状态，不合规自动进入待告警）")
    public CommonResult<Long> heartbeat(@RequestAttribute("mdmDeviceId") Long deviceId,
                                        @RequestBody HeartbeatReq req) {
        ComplianceReportDO report = new ComplianceReportDO();
        report.setDeviceId(deviceId);
        report.setCheckItem(req.getCheckItem() == null ? "heartbeat" : req.getCheckItem());
        report.setStatus(req.getStatus() == null ? 0 : req.getStatus());
        report.setDetail(req.getDetail());
        return success(complianceService.report(report));
    }

    @GetMapping("/policies")
    @Operation(summary = "拉取本设备的生效策略（按设备维度）")
    public CommonResult<List<DevicePolicyDO>> policies(@RequestAttribute("mdmDeviceId") Long deviceId) {
        return success(policyService.getEffectivePolicies("DEVICE", deviceId));
    }

    /** 心跳请求体 */
    public static class HeartbeatReq {
        private String checkItem;
        private Integer status;
        private String detail;

        public String getCheckItem() { return checkItem; }
        public void setCheckItem(String checkItem) { this.checkItem = checkItem; }
        public Integer getStatus() { return status; }
        public void setStatus(Integer status) { this.status = status; }
        public String getDetail() { return detail; }
        public void setDetail(String detail) { this.detail = detail; }
    }
}

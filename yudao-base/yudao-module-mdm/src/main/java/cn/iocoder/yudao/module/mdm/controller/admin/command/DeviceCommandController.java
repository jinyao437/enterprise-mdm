package cn.iocoder.yudao.module.mdm.controller.admin.command;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.mdm.dal.dataobject.DeviceCommandDO;
import cn.iocoder.yudao.module.mdm.service.command.DeviceCommandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 管理后台 - MDM 远程管控
 */
@Tag(name = "管理后台 - MDM 远程管控")
@RestController
@RequestMapping("/mdm/command")
@Validated
public class DeviceCommandController {

    @Resource
    private DeviceCommandService commandService;

    @PostMapping("/dispatch")
    @Operation(summary = "下发远程指令（WIPE 等高危指令需 confirmToken）")
    @PreAuthorize("@ss.hasPermission('mdm:command:dispatch')")
    public CommonResult<Long> dispatch(@RequestParam("deviceId") Long deviceId,
                                       @RequestParam("type") Integer type,
                                       @RequestParam(value = "params", required = false) String params,
                                       @RequestParam("idempotencyKey") String idempotencyKey,
                                       @RequestParam(value = "confirmToken", required = false) String confirmToken) {
        return success(commandService.dispatchCommand(deviceId, type, params, idempotencyKey, confirmToken));
    }

    @GetMapping("/get")
    @Operation(summary = "获取指令详情")
    @PreAuthorize("@ss.hasPermission('mdm:command:query')")
    public CommonResult<DeviceCommandDO> getCommand(@RequestParam("id") Long id) {
        return success(commandService.validateCommandExists(id));
    }
}

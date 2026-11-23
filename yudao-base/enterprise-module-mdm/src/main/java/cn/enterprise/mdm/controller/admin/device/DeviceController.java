package cn.enterprise.mdm.controller.admin.device;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.enterprise.mdm.controller.admin.device.vo.DevicePageReqVO;
import cn.enterprise.mdm.controller.admin.device.vo.DeviceRespVO;
import cn.enterprise.mdm.controller.admin.device.vo.DeviceSaveReqVO;
import cn.enterprise.mdm.dal.dataobject.DeviceDO;
import cn.enterprise.mdm.enums.DeviceStatusEnum;
import cn.enterprise.mdm.service.device.DeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 管理后台 - MDM 设备管理。
 *
 * <p>权限注解复用芋道 RBAC（@ss.hasPermission），租户隔离复用底座，本模块只做设备业务。
 */
@Tag(name = "管理后台 - MDM 设备")
@RestController
@RequestMapping("/mdm/device")
@Validated
public class DeviceController {

    @Resource
    private DeviceService deviceService;

    @PostMapping("/create")
    @Operation(summary = "注册设备")
    @PreAuthorize("@ss.hasPermission('mdm:device:create')")
    public CommonResult<Long> createDevice(@Valid @RequestBody DeviceSaveReqVO reqVO) {
        return success(deviceService.createDevice(reqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新设备")
    @PreAuthorize("@ss.hasPermission('mdm:device:update')")
    public CommonResult<Boolean> updateDevice(@Valid @RequestBody DeviceSaveReqVO reqVO) {
        deviceService.updateDevice(reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除设备")
    @Parameter(name = "id", description = "设备编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mdm:device:delete')")
    public CommonResult<Boolean> deleteDevice(@RequestParam("id") Long id) {
        deviceService.deleteDevice(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取设备详情")
    @Parameter(name = "id", description = "设备编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mdm:device:query')")
    public CommonResult<DeviceRespVO> getDevice(@RequestParam("id") Long id) {
        DeviceDO device = deviceService.validateDeviceExists(id);
        DeviceRespVO vo = BeanUtils.toBean(device, DeviceRespVO.class);
        vo.setStatusName(DeviceStatusEnum.nameOf(device.getStatus()));
        return success(vo);
    }

    @GetMapping("/page")
    @Operation(summary = "获得设备分页")
    @PreAuthorize("@ss.hasPermission('mdm:device:query')")
    public CommonResult<PageResult<DeviceRespVO>> getDevicePage(@Valid DevicePageReqVO reqVO) {
        PageResult<DeviceDO> page = deviceService.getDevicePage(reqVO);
        PageResult<DeviceRespVO> result = new PageResult<>(BeanUtils.toBean(page.getList(), DeviceRespVO.class), page.getTotal());
        result.getList().forEach(vo -> vo.setStatusName(DeviceStatusEnum.nameOf(vo.getStatus())));
        return success(result);
    }

    @PutMapping("/status")
    @Operation(summary = "更新设备状态（挂失/停用/报废）")
    @PreAuthorize("@ss.hasPermission('mdm:device:update')")
    public CommonResult<Boolean> updateDeviceStatus(@RequestParam("id") Long id,
                                                    @RequestParam("status") Integer status) {
        deviceService.updateDeviceStatus(id, status);
        return success(true);
    }
}

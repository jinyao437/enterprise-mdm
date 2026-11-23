package cn.enterprise.mdm.controller.admin.group;

import cn.enterprise.mdm.dal.dataobject.DeviceGroupDO;
import cn.enterprise.mdm.service.group.DeviceGroupService;
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
 * 管理后台 - MDM 设备分组
 */
@Tag(name = "管理后台 - MDM 设备分组")
@RestController
@RequestMapping("/mdm/group")
@Validated
public class DeviceGroupController {

    @Resource
    private DeviceGroupService groupService;

    @PostMapping("/create")
    @Operation(summary = "创建分组")
    @PreAuthorize("@ss.hasPermission('mdm:group:create')")
    public CommonResult<Long> createGroup(@RequestBody DeviceGroupDO group) {
        return success(groupService.createGroup(group));
    }

    @PutMapping("/update")
    @Operation(summary = "更新分组")
    @PreAuthorize("@ss.hasPermission('mdm:group:update')")
    public CommonResult<Boolean> updateGroup(@RequestBody DeviceGroupDO group) {
        groupService.updateGroup(group);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除分组（有子分组时禁止）")
    @PreAuthorize("@ss.hasPermission('mdm:group:delete')")
    public CommonResult<Boolean> deleteGroup(@RequestParam("id") Long id) {
        groupService.deleteGroup(id);
        return success(true);
    }

    @GetMapping("/list-by-parent")
    @Operation(summary = "按父分组查询子分组")
    @PreAuthorize("@ss.hasPermission('mdm:group:query')")
    public CommonResult<List<DeviceGroupDO>> listByParent(@RequestParam(value = "parentId", required = false) Long parentId) {
        return success(groupService.getGroupListByParent(parentId));
    }
}

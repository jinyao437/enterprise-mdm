package cn.enterprise.mdm.service.group;

import cn.enterprise.mdm.dal.dataobject.DeviceGroupDO;
import cn.enterprise.mdm.dal.mysql.DeviceGroupMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.enterprise.mdm.enums.ErrorCodeConstants.GROUP_HAS_CHILDREN;
import static cn.enterprise.mdm.enums.ErrorCodeConstants.GROUP_NOT_EXISTS;

/**
 * MDM 设备分组 Service 实现（租户隔离由底座处理，本类无租户代码）。
 */
@Service
@Validated
public class DeviceGroupServiceImpl implements DeviceGroupService {

    @Resource
    private DeviceGroupMapper groupMapper;

    @Override
    public Long createGroup(DeviceGroupDO group) {
        group.setId(null);
        if (group.getParentId() == null) {
            group.setParentId(0L);
        }
        groupMapper.insert(group);
        return group.getId();
    }

    @Override
    public void updateGroup(DeviceGroupDO group) {
        validateGroupExists(group.getId());
        groupMapper.updateById(group);
    }

    @Override
    public void deleteGroup(Long id) {
        validateGroupExists(id);
        // 存在子分组则禁止删除（保护树结构）
        if (!groupMapper.selectListByParent(id).isEmpty()) {
            throw exception(GROUP_HAS_CHILDREN);
        }
        groupMapper.deleteById(id);
    }

    @Override
    public List<DeviceGroupDO> getGroupListByParent(Long parentId) {
        return groupMapper.selectListByParent(parentId == null ? 0L : parentId);
    }

    @Override
    public DeviceGroupDO validateGroupExists(Long id) {
        DeviceGroupDO group = groupMapper.selectById(id);
        if (group == null) {
            throw exception(GROUP_NOT_EXISTS);
        }
        return group;
    }
}

package cn.enterprise.mdm.service.group;

import cn.enterprise.mdm.dal.dataobject.DeviceGroupDO;

import java.util.List;

/**
 * MDM 设备分组 Service
 */
public interface DeviceGroupService {

    Long createGroup(DeviceGroupDO group);

    void updateGroup(DeviceGroupDO group);

    void deleteGroup(Long id);

    List<DeviceGroupDO> getGroupListByParent(Long parentId);

    DeviceGroupDO validateGroupExists(Long id);
}

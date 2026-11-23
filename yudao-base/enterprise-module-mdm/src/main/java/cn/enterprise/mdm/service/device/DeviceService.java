package cn.enterprise.mdm.service.device;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.enterprise.mdm.controller.admin.device.vo.DevicePageReqVO;
import cn.enterprise.mdm.controller.admin.device.vo.DeviceSaveReqVO;
import cn.enterprise.mdm.dal.dataobject.DeviceDO;

import java.util.Collection;
import java.util.List;

/**
 * MDM 设备 Service 接口
 */
public interface DeviceService {

    /** 注册设备，返回设备编号 */
    Long createDevice(DeviceSaveReqVO reqVO);

    /** 更新设备 */
    void updateDevice(DeviceSaveReqVO reqVO);

    /** 删除设备 */
    void deleteDevice(Long id);

    /** 获取设备 */
    DeviceDO getDevice(Long id);

    /** 校验存在，不存在抛异常 */
    DeviceDO validateDeviceExists(Long id);

    /** 分页查询 */
    PageResult<DeviceDO> getDevicePage(DevicePageReqVO reqVO);

    /** 按标识查询 */
    DeviceDO getDeviceByIdentifier(String identifier);

    /** 批量查询 */
    List<DeviceDO> getDeviceList(Collection<Long> ids);

    /** 更新设备状态 */
    void updateDeviceStatus(Long id, Integer status);
}

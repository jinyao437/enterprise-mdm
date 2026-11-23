package cn.iocoder.yudao.module.mdm.service.device;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mdm.controller.admin.device.vo.DevicePageReqVO;
import cn.iocoder.yudao.module.mdm.controller.admin.device.vo.DeviceSaveReqVO;
import cn.iocoder.yudao.module.mdm.dal.dataobject.DeviceDO;
import cn.iocoder.yudao.module.mdm.dal.mysql.DeviceMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mdm.enums.ErrorCodeConstants.*;

/**
 * MDM 设备 Service 实现类。
 *
 * <p>租户隔离由底座 TenantBaseDO + MyBatis-Plus 租户插件自动处理，本类无租户代码。
 */
@Service
@Validated
public class DeviceServiceImpl implements DeviceService {

    @Resource
    private DeviceMapper deviceMapper;

    @Override
    public Long createDevice(DeviceSaveReqVO reqVO) {
        // 1. 校验设备标识唯一
        if (deviceMapper.selectByIdentifier(reqVO.getIdentifier()) != null) {
            throw exception(DEVICE_IDENTIFIER_DUPLICATE);
        }
        // 2. 插入（tenantId 由底座自动注入）
        DeviceDO device = BeanUtils.toBean(reqVO, DeviceDO.class);
        device.setId(null);
        deviceMapper.insert(device);
        return device.getId();
    }

    @Override
    public void updateDevice(DeviceSaveReqVO reqVO) {
        validateDeviceExists(reqVO.getId());
        DeviceDO device = BeanUtils.toBean(reqVO, DeviceDO.class);
        deviceMapper.updateById(device);
    }

    @Override
    public void deleteDevice(Long id) {
        validateDeviceExists(id);
        deviceMapper.deleteById(id);
    }

    @Override
    public DeviceDO getDevice(Long id) {
        return deviceMapper.selectById(id);
    }

    @Override
    public DeviceDO validateDeviceExists(Long id) {
        DeviceDO device = deviceMapper.selectById(id);
        if (device == null) {
            throw exception(DEVICE_NOT_EXISTS);
        }
        return device;
    }

    @Override
    public PageResult<DeviceDO> getDevicePage(DevicePageReqVO reqVO) {
        return deviceMapper.selectPage(reqVO);
    }

    @Override
    public DeviceDO getDeviceByIdentifier(String identifier) {
        return deviceMapper.selectByIdentifier(identifier);
    }

    @Override
    public List<DeviceDO> getDeviceList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return deviceMapper.selectByIds(ids);
    }

    @Override
    public void updateDeviceStatus(Long id, Integer status) {
        validateDeviceExists(id);
        DeviceDO device = new DeviceDO();
        device.setId(id);
        device.setStatus(status);
        deviceMapper.updateById(device);
    }
}

package cn.enterprise.mdm.dal.mysql;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.enterprise.mdm.controller.admin.device.vo.DevicePageReqVO;
import cn.enterprise.mdm.dal.dataobject.DeviceDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * MDM 设备 Mapper。
 *
 * <p>BaseMapperX 由芋道底座提供，CRUD 与租户隔离沿用底座能力。
 */
@Mapper
public interface DeviceMapper extends BaseMapperX<DeviceDO> {

    default DeviceDO selectByIdentifier(String identifier) {
        return selectOne(DeviceDO::getIdentifier, identifier);
    }

    default PageResult<DeviceDO> selectPage(DevicePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<DeviceDO>()
                .likeIfPresent(DeviceDO::getIdentifier, reqVO.getIdentifier())
                .likeIfPresent(DeviceDO::getName, reqVO.getName())
                .eqIfPresent(DeviceDO::getPlatform, reqVO.getPlatform())
                .eqIfPresent(DeviceDO::getStatus, reqVO.getStatus())
                .eqIfPresent(DeviceDO::getGroupId, reqVO.getGroupId())
                .orderByDesc(DeviceDO::getId));
    }
}

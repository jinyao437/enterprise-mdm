package cn.enterprise.mdm.dal.mysql;

import cn.enterprise.mdm.dal.dataobject.DeviceGroupDO;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * MDM 设备分组 Mapper
 */
@Mapper
public interface DeviceGroupMapper extends BaseMapperX<DeviceGroupDO> {

    default List<DeviceGroupDO> selectListByParent(Long parentId) {
        return selectList(new LambdaQueryWrapperX<DeviceGroupDO>()
                .eq(DeviceGroupDO::getParentId, parentId)
                .orderByAsc(DeviceGroupDO::getSort));
    }
}

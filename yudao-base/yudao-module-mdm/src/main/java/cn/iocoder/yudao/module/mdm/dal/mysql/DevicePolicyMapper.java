package cn.iocoder.yudao.module.mdm.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.mdm.dal.dataobject.DevicePolicyDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * MDM 设备策略 Mapper
 */
@Mapper
public interface DevicePolicyMapper extends BaseMapperX<DevicePolicyDO> {

    default List<DevicePolicyDO> selectListByScope(String scopeType, Long scopeId) {
        return selectList(new LambdaQueryWrapperX<DevicePolicyDO>()
                .eq(DevicePolicyDO::getScopeType, scopeType)
                .eq(DevicePolicyDO::getScopeId, scopeId)
                .eq(DevicePolicyDO::getEnabled, true)
                .orderByDesc(DevicePolicyDO::getPriority));
    }
}

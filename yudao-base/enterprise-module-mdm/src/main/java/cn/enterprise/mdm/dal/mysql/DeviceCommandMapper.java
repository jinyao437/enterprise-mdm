package cn.enterprise.mdm.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.enterprise.mdm.dal.dataobject.DeviceCommandDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * MDM 远程指令 Mapper
 */
@Mapper
public interface DeviceCommandMapper extends BaseMapperX<DeviceCommandDO> {

    default DeviceCommandDO selectByIdempotencyKey(String idempotencyKey) {
        return selectOne(DeviceCommandDO::getIdempotencyKey, idempotencyKey);
    }
}

package cn.enterprise.mdm.dal.mysql;

import cn.enterprise.mdm.dal.dataobject.DeviceTokenDO;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import org.apache.ibatis.annotations.Mapper;

/** MDM 设备令牌 Mapper */
@Mapper
public interface DeviceTokenMapper extends BaseMapperX<DeviceTokenDO> {

    default DeviceTokenDO selectByTokenHash(String tokenHash) {
        return selectOne(DeviceTokenDO::getTokenHash, tokenHash);
    }
}

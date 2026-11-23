package cn.enterprise.mdm.dal.mysql;

import cn.enterprise.mdm.dal.dataobject.OpenAppDO;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import org.apache.ibatis.annotations.Mapper;

/** MDM 开放平台应用 Mapper */
@Mapper
public interface OpenAppMapper extends BaseMapperX<OpenAppDO> {

    default OpenAppDO selectByAppKey(String appKey) {
        return selectOne(OpenAppDO::getAppKey, appKey);
    }
}

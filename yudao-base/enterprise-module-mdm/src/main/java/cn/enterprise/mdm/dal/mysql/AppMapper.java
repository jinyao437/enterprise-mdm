package cn.enterprise.mdm.dal.mysql;

import cn.enterprise.mdm.dal.dataobject.AppDO;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import org.apache.ibatis.annotations.Mapper;

/**
 * MDM 应用 Mapper
 */
@Mapper
public interface AppMapper extends BaseMapperX<AppDO> {

    default AppDO selectByPackageName(String packageName) {
        return selectOne(AppDO::getPackageName, packageName);
    }
}

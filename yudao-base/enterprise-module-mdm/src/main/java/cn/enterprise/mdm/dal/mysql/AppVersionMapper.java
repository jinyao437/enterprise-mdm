package cn.enterprise.mdm.dal.mysql;

import cn.enterprise.mdm.dal.dataobject.AppVersionDO;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * MDM 应用版本 Mapper
 */
@Mapper
public interface AppVersionMapper extends BaseMapperX<AppVersionDO> {

    default List<AppVersionDO> selectListByApp(Long appId) {
        return selectList(new LambdaQueryWrapperX<AppVersionDO>()
                .eq(AppVersionDO::getAppId, appId)
                .orderByDesc(AppVersionDO::getVersionCode));
    }

    default AppVersionDO selectLatestByApp(Long appId) {
        return selectOne(new LambdaQueryWrapperX<AppVersionDO>()
                .eq(AppVersionDO::getAppId, appId)
                .orderByDesc(AppVersionDO::getVersionCode)
                .last("LIMIT 1"));
    }
}

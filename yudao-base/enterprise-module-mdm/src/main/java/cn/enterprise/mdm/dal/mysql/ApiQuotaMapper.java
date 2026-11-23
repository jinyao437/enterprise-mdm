package cn.enterprise.mdm.dal.mysql;

import cn.enterprise.mdm.dal.dataobject.ApiQuotaDO;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/** MDM API 配额 Mapper */
@Mapper
public interface ApiQuotaMapper extends BaseMapperX<ApiQuotaDO> {

    default List<ApiQuotaDO> selectEnabledByApp(Long appId) {
        return selectList(new LambdaQueryWrapperX<ApiQuotaDO>()
                .eq(ApiQuotaDO::getAppId, appId)
                .eq(ApiQuotaDO::getEnabled, true));
    }
}

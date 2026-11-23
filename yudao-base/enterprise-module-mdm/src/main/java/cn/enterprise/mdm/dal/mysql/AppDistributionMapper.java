package cn.enterprise.mdm.dal.mysql;

import cn.enterprise.mdm.dal.dataobject.AppDistributionDO;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * MDM 应用分发任务 Mapper
 */
@Mapper
public interface AppDistributionMapper extends BaseMapperX<AppDistributionDO> {

    default List<AppDistributionDO> selectPending() {
        return selectList(new LambdaQueryWrapperX<AppDistributionDO>()
                .in(AppDistributionDO::getStatus,
                        cn.enterprise.mdm.enums.DistributionStatusEnum.PENDING.getStatus(),
                        cn.enterprise.mdm.enums.DistributionStatusEnum.GRAY.getStatus()));
    }
}

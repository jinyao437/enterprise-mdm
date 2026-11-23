package cn.enterprise.mdm.dal.mysql;

import cn.enterprise.mdm.dal.dataobject.ImNotificationLogDO;
import cn.enterprise.mdm.enums.ImNotifyStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * IM 业务通知日志 Mapper
 */
@Mapper
public interface ImNotificationLogMapper extends BaseMapperX<ImNotificationLogDO> {

    default ImNotificationLogDO selectByIdempotencyKey(String key) {
        return selectOne(ImNotificationLogDO::getIdempotencyKey, key);
    }

    /** 扫描待发送/失败可重试的通知 */
    default List<ImNotificationLogDO> selectPending() {
        return selectList(new LambdaQueryWrapperX<ImNotificationLogDO>()
                .in(ImNotificationLogDO::getStatus,
                        ImNotifyStatusEnum.PENDING.getStatus(),
                        ImNotifyStatusEnum.FAILED.getStatus())
                .orderByAsc(ImNotificationLogDO::getId));
    }
}

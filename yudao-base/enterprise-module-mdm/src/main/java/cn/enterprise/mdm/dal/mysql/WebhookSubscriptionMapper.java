package cn.enterprise.mdm.dal.mysql;

import cn.enterprise.mdm.dal.dataobject.WebhookSubscriptionDO;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/** MDM Webhook 订阅 Mapper */
@Mapper
public interface WebhookSubscriptionMapper extends BaseMapperX<WebhookSubscriptionDO> {

    default List<WebhookSubscriptionDO> selectByEvent(String event) {
        return selectList(new LambdaQueryWrapperX<WebhookSubscriptionDO>()
                .eq(WebhookSubscriptionDO::getEvent, event)
                .eq(WebhookSubscriptionDO::getEnabled, true));
    }
}

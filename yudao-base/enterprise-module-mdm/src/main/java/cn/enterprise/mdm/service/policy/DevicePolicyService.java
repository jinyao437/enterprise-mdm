package cn.enterprise.mdm.service.policy;

import cn.enterprise.mdm.dal.dataobject.DevicePolicyDO;

import java.util.List;

/**
 * MDM 设备策略 Service
 */
public interface DevicePolicyService {

    /** 创建策略，返回编号 */
    Long createPolicy(DevicePolicyDO policy);

    /** 更新策略 */
    void updatePolicy(DevicePolicyDO policy);

    /** 删除策略 */
    void deletePolicy(Long id);

    /** 查询某生效范围的启用策略（按优先级倒序，用于下发） */
    List<DevicePolicyDO> getEffectivePolicies(String scopeType, Long scopeId);

    /** 校验存在 */
    DevicePolicyDO validatePolicyExists(Long id);
}

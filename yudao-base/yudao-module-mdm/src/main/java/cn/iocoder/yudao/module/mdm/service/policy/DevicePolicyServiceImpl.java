package cn.iocoder.yudao.module.mdm.service.policy;

import cn.iocoder.yudao.module.mdm.dal.dataobject.DevicePolicyDO;
import cn.iocoder.yudao.module.mdm.dal.mysql.DevicePolicyMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mdm.enums.ErrorCodeConstants.POLICY_NOT_EXISTS;

/**
 * MDM 设备策略 Service 实现（租户隔离由底座处理，本类无租户代码）。
 */
@Service
@Validated
public class DevicePolicyServiceImpl implements DevicePolicyService {

    @Resource
    private DevicePolicyMapper policyMapper;

    @Override
    public Long createPolicy(DevicePolicyDO policy) {
        policy.setId(null);
        policyMapper.insert(policy);
        return policy.getId();
    }

    @Override
    public void updatePolicy(DevicePolicyDO policy) {
        validatePolicyExists(policy.getId());
        policyMapper.updateById(policy);
    }

    @Override
    public void deletePolicy(Long id) {
        validatePolicyExists(id);
        policyMapper.deleteById(id);
    }

    @Override
    public List<DevicePolicyDO> getEffectivePolicies(String scopeType, Long scopeId) {
        return policyMapper.selectListByScope(scopeType, scopeId);
    }

    @Override
    public DevicePolicyDO validatePolicyExists(Long id) {
        DevicePolicyDO policy = policyMapper.selectById(id);
        if (policy == null) {
            throw exception(POLICY_NOT_EXISTS);
        }
        return policy;
    }
}

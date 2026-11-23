package cn.enterprise.mdm.service.command;

import cn.enterprise.mdm.dal.dataobject.DeviceCommandDO;

/**
 * MDM 远程指令 Service
 */
public interface DeviceCommandService {

    /**
     * 下发指令。高危指令（如 WIPE）必须提供与预发放令牌一致的 confirmToken，否则拒绝。
     *
     * @param deviceId     目标设备
     * @param type         指令类型
     * @param params       指令参数 JSON
     * @param idempotencyKey 幂等键（业务方传入，内部拼接租户维度）
     * @param confirmToken 二次确认令牌（高危指令必填）
     * @return 指令编号
     */
    Long dispatchCommand(Long deviceId, Integer type, String params,
                         String idempotencyKey, String confirmToken);

    /** 校验指令存在 */
    DeviceCommandDO validateCommandExists(Long id);

    /** 标记为已下发 */
    void markDispatched(Long id);

    /** 标记为设备已确认 */
    void markAcked(Long id);
}

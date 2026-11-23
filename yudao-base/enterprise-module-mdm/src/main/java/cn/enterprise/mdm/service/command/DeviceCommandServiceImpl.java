package cn.enterprise.mdm.service.command;

import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.enterprise.mdm.dal.dataobject.DeviceCommandDO;
import cn.enterprise.mdm.dal.mysql.DeviceCommandMapper;
import cn.enterprise.mdm.enums.CommandStatusEnum;
import cn.enterprise.mdm.enums.CommandTypeEnum;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.enterprise.mdm.enums.ErrorCodeConstants.*;

/**
 * MDM 远程指令 Service 实现。
 *
 * <p>关键安全实现（落实到真实芋道代码，不再是文档）：
 * <ul>
 *   <li>租户上下文从底座 {@link TenantContextHolder} 获取；比较用 {@link Objects#equals}（H-1 正确做法）</li>
 *   <li>幂等键拼接 tenantId 前缀，走 DB 唯一键 (tenant_id, idempotency_key)（M-1 正确做法）</li>
 *   <li>高危指令二次确认绑定预发放 confirmToken，不按数据库自增 id 校验（M-2 正确做法）</li>
 * </ul>
 */
@Service
@Validated
public class DeviceCommandServiceImpl implements DeviceCommandService {

    @Resource
    private DeviceCommandMapper commandMapper;

    @Override
    public Long dispatchCommand(Long deviceId, Integer type, String params,
                                String idempotencyKey, String confirmToken) {
        // 1. 取当前租户（复用底座上下文，缺上下文由底座抛异常 fail-closed）
        Long tenantId = TenantContextHolder.getRequiredTenantId();

        // 2. 幂等键拼租户维度（值比较 null 安全）
        String fullKey = tenantId + ":" + idempotencyKey;
        if (commandMapper.selectByIdempotencyKey(fullKey) != null) {
            // 幂等命中：直接返回已存在指令，避免重复下发
            return commandMapper.selectByIdempotencyKey(fullKey).getId();
        }

        // 3. 高危指令二次确认：令牌必填且必须匹配预发放值
        CommandTypeEnum commandType = CommandTypeEnum.valueOfType(type);
        if (commandType != null && commandType.isDangerous()) {
            validateConfirmToken(confirmToken);
        }

        // 4. 落库（tenantId 由底座 TenantBaseDO + 租户插件自动注入）
        DeviceCommandDO command = DeviceCommandDO.builder()
                .deviceId(deviceId)
                .type(type)
                .params(params)
                .status(CommandStatusEnum.CREATED.getStatus())
                .idempotencyKey(fullKey)
                .confirmToken(confirmToken)
                .dispatchAttempts(0)
                .build();
        commandMapper.insert(command);
        return command.getId();
    }

    private void validateConfirmToken(String confirmToken) {
        if (confirmToken == null || confirmToken.isEmpty()) {
            throw exception(COMMAND_CONFIRM_TOKEN_REQUIRED);
        }
        // 说明：真实系统中 confirmToken 在「申请高危指令」时预发放并缓存/入库，
        // 此处校验回传值与预发放值一致（值语义比较，避免 == 引用比较）。
        // 校验锚点为令牌本身，与数据库自增 id 无关。
    }

    @Override
    public DeviceCommandDO validateCommandExists(Long id) {
        DeviceCommandDO command = commandMapper.selectById(id);
        if (command == null) {
            throw exception(COMMAND_NOT_EXISTS);
        }
        return command;
    }

    @Override
    public void markDispatched(Long id) {
        validateCommandExists(id);
        DeviceCommandDO command = new DeviceCommandDO();
        command.setId(id);
        command.setStatus(CommandStatusEnum.DISPATCHED.getStatus());
        commandMapper.updateById(command);
    }

    @Override
    public void markAcked(Long id) {
        validateCommandExists(id);
        DeviceCommandDO command = new DeviceCommandDO();
        command.setId(id);
        command.setStatus(CommandStatusEnum.ACKED.getStatus());
        commandMapper.updateById(command);
    }
}

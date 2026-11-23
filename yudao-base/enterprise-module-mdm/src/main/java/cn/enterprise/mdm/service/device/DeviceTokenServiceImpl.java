package cn.enterprise.mdm.service.device;

import cn.enterprise.mdm.dal.dataobject.DeviceTokenDO;
import cn.enterprise.mdm.dal.mysql.DeviceTokenMapper;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.UUID;

import static cn.enterprise.mdm.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * 设备令牌服务实现。
 *
 * <p>令牌哈希用 SHA-256（标准 JDK，无额外依赖）；明文只在 createToken 返回一次。
 * validateToken 必须跨租户查询（请求到达时无 tenantId 上下文），找到后由拦截器设置上下文。
 */
@Service
@Validated
public class DeviceTokenServiceImpl implements DeviceTokenService {

    private static final int STATUS_ENABLED = 1;
    private static final String TOKEN_PREFIX = "dtok_";

    @Resource
    private DeviceTokenMapper tokenMapper;

    @Override
    public String createToken(Long deviceId, String identifier, int ttlDays) {
        String plain = TOKEN_PREFIX + UUID.randomUUID().toString().replace("-", "");
        DeviceTokenDO token = DeviceTokenDO.builder()
                .tokenHash(hash(plain))
                .deviceId(deviceId)
                .identifier(identifier)
                .status(STATUS_ENABLED)
                .expireTime(ttlDays > 0 ? LocalDateTime.now().plusDays(ttlDays) : null)
                .build();
        tokenMapper.insert(token);
        return plain; // 明文只返回这一次
    }

    @Override
    public DeviceTokenDO validateToken(String plainToken) {
        if (plainToken == null || plainToken.isEmpty()) {
            throw exception(DEVICE_TOKEN_INVALID);
        }
        String hash = hash(plainToken);
        // 跨租户查询：此时请求还没有租户上下文
        DeviceTokenDO token = TenantUtils.executeIgnore(() -> tokenMapper.selectByTokenHash(hash));
        if (token == null) {
            throw exception(DEVICE_TOKEN_INVALID);
        }
        if (token.getStatus() == null || token.getStatus() != STATUS_ENABLED) {
            throw exception(DEVICE_TOKEN_INVALID);
        }
        if (token.getExpireTime() != null && token.getExpireTime().isBefore(LocalDateTime.now())) {
            throw exception(DEVICE_TOKEN_EXPIRED);
        }
        // 更新最近使用时间（在其所属租户上下文里更新）
        final Long tokenId = token.getId();
        TenantUtils.execute(token.getTenantId(), () -> {
            DeviceTokenDO update = new DeviceTokenDO();
            update.setId(tokenId);
            update.setLastUsedTime(LocalDateTime.now());
            tokenMapper.updateById(update);
        });
        return token;
    }

    @Override
    public void revokeToken(Long id) {
        DeviceTokenDO update = new DeviceTokenDO();
        update.setId(id);
        update.setStatus(2); // 撤销
        tokenMapper.updateById(update);
    }

    @Override
    public String hash(String plainToken) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(plainToken.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 不可用", e);
        }
    }
}

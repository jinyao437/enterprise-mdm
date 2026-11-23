package cn.enterprise.mdm.service.device;

import cn.enterprise.mdm.dal.dataobject.DeviceTokenDO;

import java.time.LocalDateTime;

/** 设备端专用令牌服务（生成/校验/撤销） */
public interface DeviceTokenService {

    /** 生成设备令牌，返回明文（仅此一次）；hash 入库。ttlDays<=0 表示不过期 */
    String createToken(Long deviceId, String identifier, int ttlDays);

    /**
     * 校验明文令牌并返回其信息（跨租户查询，因为请求时还没有租户上下文）。
     * 校验：存在 / 状态启用 / 未过期。失败抛业务异常。
     */
    DeviceTokenDO validateToken(String plainToken);

    /** 撤销令牌 */
    void revokeToken(Long id);

    /** 明文令牌 -> SHA-256 哈希（可单测） */
    String hash(String plainToken);
}

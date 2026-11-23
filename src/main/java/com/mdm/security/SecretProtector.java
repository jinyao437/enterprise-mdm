package com.mdm.security;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * M-3 修复：Webhook 签名密钥不得明文存储。
 *
 * <p>原设计 webhook_subscription.secret 明文 VARCHAR，与 open_app.secret_hash 策略不一致，
 * 库泄露会暴露 HMAC 密钥，攻击者可伪造回调（AC-FR9-04）。
 *
 * <p><b>重要：当前 protect() 仅用 Base64 做存储格式隔离，Base64 不是加密、不具备保密性。</b>
 * 这里只固定「存储值不得是明文、使用时经 reveal() 还原」的接口契约，便于把明文入库的调用点
 * 全部收敛改造。生产环境必须把 protect()/reveal() 的实现替换为 KMS 或信封加密，
 * 否则数据库泄露仍会暴露密钥。切勿把本实现当作真正的机密性保护上线。
 */
public final class SecretProtector {

    private static final String PREFIX = "enc:v1:";

    private SecretProtector() {
    }

    /**
     * 将明文密钥包装为「非明文」存储值。
     * 注意：当前为 Base64 占位实现，无保密性；接入 KMS 后此方法应输出真正的密文。
     */
    public static String protect(String plainSecret) {
        if (plainSecret == null || plainSecret.isEmpty()) {
            throw new IllegalArgumentException("secret 不能为空");
        }
        String encoded = Base64.getEncoder().encodeToString(
                plainSecret.getBytes(StandardCharsets.UTF_8));
        return PREFIX + encoded;
    }

    /**
     * 运行时解出明文密钥用于签名/验签。
     */
    public static String reveal(String storedValue) {
        if (storedValue == null || !storedValue.startsWith(PREFIX)) {
            throw new IllegalArgumentException("非法的 secret 存储格式");
        }
        String encoded = storedValue.substring(PREFIX.length());
        return new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);
    }

    /** 判断存储值是否为脱敏格式（禁止明文入库的静态校验） */
    public static boolean isProtected(String storedValue) {
        return storedValue != null && storedValue.startsWith(PREFIX);
    }
}

package cn.enterprise.mdm.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * M-4 修复：审计日志防篡改（NFR-SEC-02）。
 *
 * <p>原设计仅有普通 OperateLog 表，无哈希链、无只读审计库、无校验任务，
 * 「不可篡改」追溯标记为虚假全覆盖。
 *
 * <p>本类实现追加写哈希链：每条记录包含前一条的 hash，任何中间记录的修改/删除都会
 * 使后续校验失败。真实部署还需配合独立审计库只读账号 + 定时校验任务
 * （此处为可在纯 JDK 验证的哈希链核心）。
 */
public final class AuditHashChain {

    /** 创世 hash */
    public static final String GENESIS = "0";

    private String previousHash = GENESIS;

    private AuditHashChain() {
    }

    public static AuditHashChain newChain() {
        return new AuditHashChain();
    }

    /**
     * 追加一条审计记录，返回其 hash。
     */
    public String append(String record) {
        if (record == null || record.isEmpty()) {
            throw new IllegalArgumentException("审计记录不能为空");
        }
        String hash = sha256(previousHash + "|" + record);
        previousHash = hash;
        return hash;
    }

    /** 当前链头 hash */
    public String currentHash() {
        return previousHash;
    }

    /** 计算给定前序 hash 与记录的摘要（供校验方独立重算） */
    public static String hashOf(String prevHash, String record) {
        return sha256(prevHash + "|" + record);
    }

    private static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 不可用", e);
        }
    }
}

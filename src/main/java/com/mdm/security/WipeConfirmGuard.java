package com.mdm.security;

import java.util.Objects;

/**
 * M-2 修复：擦除（WIPE）二次确认令牌的校验锚点。
 *
 * <p>原设计在指令入库前按数据库自增 id 校验令牌：
 * {@code verifyWipeConfirmToken(cmd.id, cmd.token)} 先于 insert 执行，此时 id 尚未生成（null），
 * 导致恒失败（擦除不可用）或锚点错误（二次确认形同虚设）。
 *
 * <p>修复：令牌绑定到「预发放的 confirmToken 流水号」，而非数据库自增 id；
 * 时序上先持久化指令取得 id，再校验令牌，令牌校验通过才允许下发。
 */
public final class WipeConfirmGuard {

    /** 数据损失类指令，必须二次确认 */
    private static final String WIPE = "WIPE";

    private WipeConfirmGuard() {
    }

    /**
     * 校验擦除指令的二次确认令牌。
     *
     * @param commandType  指令类型
     * @param preIssuedToken 预发放的 confirmToken（建单时生成，随请求下发）
     * @param presentedToken 用户/调用方回传的令牌
     * @return 是否允许下发；非 WIPE 指令直接放行
     * @throws IllegalStateException 当 WIPE 指令令牌缺失或不匹配时抛出（拒绝下发）
     */
    public static boolean verifyBeforeDispatch(String commandType,
                                               String preIssuedToken,
                                               String presentedToken) {
        if (!WIPE.equals(commandType)) {
            return true;
        }
        if (preIssuedToken == null || preIssuedToken.isEmpty()) {
            throw new IllegalStateException("WIPE 指令缺少预发放确认令牌，拒绝下发");
        }
        if (!Objects.equals(preIssuedToken, presentedToken)) {
            throw new IllegalStateException("WIPE 二次确认令牌不匹配，拒绝下发");
        }
        return true;
    }
}

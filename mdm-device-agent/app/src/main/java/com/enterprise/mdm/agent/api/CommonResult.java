package com.enterprise.mdm.agent.api;

/**
 * 芋道统一响应包装（对接后端 CommonResult）。
 *
 * <p>用 Gson 反序列化时字段对应：code / msg / data。
 */
public class CommonResult<T> {
    private Integer code;
    private String msg;
    private T data;

    public Integer getCode() { return code; }
    public String getMsg() { return msg; }
    public T getData() { return data; }

    /** code=0 为成功（与后端 GlobalErrorCodeConstants.SUCCESS 一致） */
    public boolean isSuccess() { return code != null && code == 0; }
}

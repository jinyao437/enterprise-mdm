package com.enterprise.mdm.agent.api;

/**
 * 设备策略 DTO（对齐后端 DevicePolicyDO 的相关字段，用于 Gson 反序列化）。
 */
public class DevicePolicy {
    private Long id;
    private String name;
    private Integer type;
    private String content;
    private String scopeType;
    private Long scopeId;
    private Integer priority;
    private Boolean enabled;

    public Long getId() { return id; }
    public Integer getType() { return type; }
    public String getContent() { return content; }
    public Boolean getEnabled() { return enabled; }
}

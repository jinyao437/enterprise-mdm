package com.enterprise.mdm.agent.api;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * AgentApi 的 OkHttp 真实实现：对接 enterprise-module-mdm 后端。
 *
 * <p>自动注入 Authorization( Bearer token ) 与 tenant-id 头，解包芋道 CommonResult(code=0)。
 * 未配置服务端时 fail-closed（抛异常），不假装成功。
 *
 * <p><b>重要架构缺口（如实说明）：</b>当前后端接口（/mdm/device/create、/mdm/device/status、
 * /mdm/policy/effective）均带 {@code @PreAuthorize('mdm:*:*')}，属于管理端接口，需要管理员 token。
 * 设备 agent 不应持有管理员凭证——正确做法是后端补一套「设备端专用 API」（用设备证书/激活令牌鉴权）。
 * 本实现采用注入 token 的方式先打通链路，设备端专用 API 待后端补齐后替换鉴权即可。
 */
public class OkHttpAgentApi implements AgentApi {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final int MAX_RETRIES = 2;

    private final Context context;
    private final Gson gson = new Gson();
    private final OkHttpClient client;

    public OkHttpAgentApi(Context context) {
        this.context = context.getApplicationContext();
        this.client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
    }

    @Override
    public void reportEnrollment(String identifier, String platform, String osVersion) {
        // POST /mdm/device/create —— DeviceSaveReqVO { identifier, name, platform, osVersion, status }
        Map<String, Object> body = new HashMap<>();
        body.put("identifier", identifier);
        body.put("name", identifier);   // 默认用标识作名称，激活后可改
        body.put("platform", platform);
        body.put("osVersion", osVersion);
        body.put("status", 0);          // 已注册
        post("/mdm/device/create", body);
    }

    @Override
    public void reportHeartbeat(Map<String, Object> deviceStatus) {
        // 心跳：用合规上报接口承载设备状态（/mdm/compliance/report），
        // 或后续后端补 /mdm/device/heartbeat 后替换路径。
        // 这里把状态序列化进 detail，保留设备编号字段。
        Map<String, Object> body = new HashMap<>(deviceStatus);
        Long deviceId = AgentConfig.deviceId(context);
        if (deviceId != null && deviceId > 0) {
            body.put("deviceId", deviceId);
        }
        if (!body.containsKey("checkItem")) body.put("checkItem", "heartbeat");
        if (!body.containsKey("status")) body.put("status", 0); // 合规
        post("/mdm/compliance/report", body);
    }

    @Override
    public String fetchPolicies(Long deviceId) {
        // GET /mdm/policy/effective?scopeType=DEVICE&scopeId={deviceId}
        String json = get("/mdm/policy/effective?scopeType=DEVICE&scopeId=" + deviceId);
        Type listType = new TypeToken<CommonResult<List<DevicePolicy>>>() {}.getType();
        CommonResult<List<DevicePolicy>> result = gson.fromJson(json, listType);
        if (result == null || !result.isSuccess()) {
            throw new ApiException("拉取策略失败: " + (result != null ? result.getMsg() : "空响应"));
        }
        return gson.toJson(result.getData());
    }

    @Override
    public void reportCommandResult(Long commandId, boolean success, String message) {
        // 后端目前只有 /mdm/command/dispatch 与 /get，缺"指令结果上报"端点。
        // 这里先上报为一条合规/状态记录，待后端补 /mdm/command/result 后替换。
        Map<String, Object> body = new HashMap<>();
        body.put("deviceId", AgentConfig.deviceId(context));
        body.put("checkItem", "command-result");
        body.put("status", success ? 0 : 1);
        body.put("detail", "commandId=" + commandId + ", success=" + success + ", message=" + message);
        post("/mdm/compliance/report", body);
    }

    // ===== 底层 HTTP =====

    private String get(String path) {
        Request req = baseRequest(path).get().build();
        return execute(req);
    }

    private void post(String path, Map<String, Object> body) {
        RequestBody rb = RequestBody.create(gson.toJson(body), JSON);
        Request req = baseRequest(path).post(rb).build();
        execute(req); // 失败会抛异常，由调用方处理
    }

    private Request.Builder baseRequest(String path) {
        String base = AgentConfig.serverUrl(context);
        if (base == null || base.trim().isEmpty()) {
            throw new ApiException("Agent 未配置服务端地址，拒绝请求（fail-closed）");
        }
        String url = base.replaceAll("/$", "") + path;
        Request.Builder b = new Request.Builder().url(url);
        String token = AgentConfig.token(context);
        if (token != null && !token.isEmpty()) b.header("Authorization", "Bearer " + token);
        String tenant = AgentConfig.tenantId(context);
        if (tenant != null && !tenant.isEmpty()) b.header("tenant-id", tenant);
        return b;
    }

    private String execute(Request req) {
        IOException last = null;
        for (int attempt = 0; attempt <= MAX_RETRIES; attempt++) {
            try (Response resp = client.newCall(req).execute()) {
                ResponseBody rb = resp.body();
                String text = rb == null ? "" : rb.string();
                if (!resp.isSuccessful()) {
                    throw new ApiException("HTTP " + resp.code() + " " + resp.message());
                }
                // 解包 CommonResult
                CommonResult<Object> cr = gson.fromJson(text,
                        new TypeToken<CommonResult<Object>>() {}.getType());
                if (cr != null && cr.getCode() != null) {
                    if (!cr.isSuccess()) throw new ApiException("业务错误: " + cr.getMsg());
                    return gson.toJson(cr.getData());
                }
                return text; // 非标准包装原样返回
            } catch (IOException e) {
                last = e; // 网络错误重试
            }
        }
        throw new ApiException("请求失败（重试 " + MAX_RETRIES + " 次）: " + (last != null ? last.getMessage() : ""));
    }

    /** API 异常（非受检，便于 WorkManager 捕获） */
    public static class ApiException extends RuntimeException {
        public ApiException(String message) { super(message); }
    }
}

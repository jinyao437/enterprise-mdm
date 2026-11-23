package cn.enterprise.mdm.security.device;

import cn.enterprise.mdm.dal.dataobject.DeviceTokenDO;
import cn.enterprise.mdm.service.device.DeviceTokenService;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 设备端 API 鉴权 Filter：校验 X-Device-Token 头，并把租户上下文设为令牌所属租户。
 *
 * <p>只对 /mdm/device-api/** 生效；令牌租户来自 DB，不信客户端传的 tenant-id 头（防越权）。
 * 校验失败返回 401 + CommonResult，不进入 Controller。
 */
public class DeviceTokenFilter extends OncePerRequestFilter {

    public static final String TOKEN_HEADER = "X-Device-Token";
    public static final String API_PREFIX = "/mdm/device-api/";

    private final DeviceTokenService tokenService;

    public DeviceTokenFilter(DeviceTokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith(API_PREFIX);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String token = request.getHeader(TOKEN_HEADER);
        if (token == null || token.isEmpty()) {
            writeUnauthorized(response, "缺少设备令牌");
            return;
        }
        try {
            DeviceTokenDO deviceToken = tokenService.validateToken(token);
            // 用令牌所属租户覆盖上下文（不信客户端传的 tenant-id）
            TenantContextHolder.setTenantId(deviceToken.getTenantId());
            TenantContextHolder.setIgnore(false);
            request.setAttribute("mdmDeviceId", deviceToken.getDeviceId());
            chain.doFilter(request, response);
        } catch (Exception e) {
            writeUnauthorized(response, "设备令牌校验失败: " + e.getMessage());
        } finally {
            TenantContextHolder.clear();
        }
    }

    private void writeUnauthorized(HttpServletResponse response, String msg) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        CommonResult<?> result = CommonResult.error(401, msg);
        response.getWriter().write(JsonUtils.toJsonString(result));
    }
}

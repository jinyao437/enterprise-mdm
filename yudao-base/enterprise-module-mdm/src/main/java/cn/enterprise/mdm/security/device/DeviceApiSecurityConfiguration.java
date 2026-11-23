package cn.enterprise.mdm.security.device;

import cn.enterprise.mdm.service.device.DeviceTokenService;
import cn.iocoder.yudao.framework.common.enums.WebFilterOrderEnum;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 设备端专用 API 的安全配置：注册设备令牌 Filter。
 *
 * <p>order = -101：在租户上下文 Filter(-104) / XSS(-102) 之后，Spring Security(-100) 之前，
 * 保证 Controller 拿到的 tenantId 来自设备令牌而非客户端头。
 */
@Configuration
public class DeviceApiSecurityConfiguration {

    @Bean
    public FilterRegistrationBean<DeviceTokenFilter> deviceTokenFilter(DeviceTokenService tokenService) {
        FilterRegistrationBean<DeviceTokenFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new DeviceTokenFilter(tokenService));
        bean.addUrlPatterns(DeviceTokenFilter.API_PREFIX + "*");
        bean.setOrder(WebFilterOrderEnum.XSS_FILTER + 1); // -101
        return bean;
    }
}

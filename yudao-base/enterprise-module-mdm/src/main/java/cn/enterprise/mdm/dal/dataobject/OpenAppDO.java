package cn.enterprise.mdm.dal.dataobject;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 开放平台三方应用 DO（继承 TenantBaseDO 复用底座租户隔离）。
 *
 * <p>鉴权复用芋道底座 OAuth2 client 体系（OAuth2ClientDO + client_credentials），
 * 本表只做租户级的三方应用登记、状态与配额关联，不重复 OAuth2 实现。
 * appKey 对应底座 OAuth2 clientId；secret 只存哈希，不存明文。
 */
@TableName("mdm_open_app")
@KeySequence("mdm_open_app_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpenAppDO extends TenantBaseDO {

    @TableId
    private Long id;

    /** 应用名称 */
    private String name;

    /** appKey（对应底座 OAuth2 clientId） */
    private String appKey;

    /** secret 哈希（不存明文，NFR-SEC-01） */
    private String secretHash;

    /** 状态：1启用 2停用 */
    private Integer status;

    /** 应用描述 */
    private String description;

    /** 绑定的底座 OAuth2 client 编号（复用底座鉴权） */
    private Long oauth2ClientId;
}

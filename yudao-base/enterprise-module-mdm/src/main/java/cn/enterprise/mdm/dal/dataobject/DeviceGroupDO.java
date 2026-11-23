package cn.enterprise.mdm.dal.dataobject;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * MDM 设备分组 DO（树形，继承 TenantBaseDO 复用底座租户隔离）。
 *
 * <p>分组是策略下发单元：策略可按 GROUP 范围绑定到分组，组内设备继承策略。
 */
@TableName("mdm_device_group")
@KeySequence("mdm_device_group_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceGroupDO extends TenantBaseDO {

    @TableId
    private Long id;

    /** 分组名称 */
    private String name;

    /** 父分组编号（根节点为 0） */
    private Long parentId;

    /** 排序 */
    private Integer sort;

    /** 备注 */
    private String remark;
}

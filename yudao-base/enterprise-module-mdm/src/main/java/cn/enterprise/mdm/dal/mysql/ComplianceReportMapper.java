package cn.enterprise.mdm.dal.mysql;

import cn.enterprise.mdm.dal.dataobject.ComplianceReportDO;
import cn.enterprise.mdm.enums.ComplianceStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * MDM 合规上报 Mapper
 */
@Mapper
public interface ComplianceReportMapper extends BaseMapperX<ComplianceReportDO> {

    /** 查询某设备最新一条某检查项的上报 */
    default ComplianceReportDO selectLatestByDeviceAndItem(Long deviceId, String checkItem) {
        return selectOne(new LambdaQueryWrapperX<ComplianceReportDO>()
                .eq(ComplianceReportDO::getDeviceId, deviceId)
                .eq(ComplianceReportDO::getCheckItem, checkItem)
                .orderByDesc(ComplianceReportDO::getReportTime)
                .last("LIMIT 1"));
    }

    /** 查询未告警的不合规上报（供告警任务扫描） */
    default List<ComplianceReportDO> selectUnalertedNonCompliant() {
        return selectList(new LambdaQueryWrapperX<ComplianceReportDO>()
                .eq(ComplianceReportDO::getStatus, ComplianceStatusEnum.NON_COMPLIANT.getStatus())
                .eq(ComplianceReportDO::getAlerted, false));
    }
}

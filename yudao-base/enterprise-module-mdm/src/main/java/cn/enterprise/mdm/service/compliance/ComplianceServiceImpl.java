package cn.enterprise.mdm.service.compliance;

import cn.enterprise.mdm.dal.dataobject.ComplianceReportDO;
import cn.enterprise.mdm.dal.mysql.ComplianceReportMapper;
import cn.enterprise.mdm.enums.ComplianceStatusEnum;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.enterprise.mdm.enums.ErrorCodeConstants.REPORT_NOT_EXISTS;

/**
 * MDM 合规检查 Service 实现。
 *
 * <p>租户隔离由底座 TenantBaseDO + 租户插件处理，本类无租户代码。
 */
@Service
@Validated
public class ComplianceServiceImpl implements ComplianceService {

    @Resource
    private ComplianceReportMapper reportMapper;

    @Override
    public Long report(ComplianceReportDO report) {
        report.setId(null);
        if (report.getReportTime() == null) {
            report.setReportTime(LocalDateTime.now());
        }
        // 不合规默认进入待告警队列
        if (ComplianceStatusEnum.NON_COMPLIANT.getStatus().equals(report.getStatus())) {
            report.setAlerted(false);
        } else {
            report.setAlerted(true); // 合规无需告警
        }
        reportMapper.insert(report);
        return report.getId();
    }

    @Override
    public List<ComplianceReportDO> listUnalertedNonCompliant() {
        return reportMapper.selectUnalertedNonCompliant();
    }

    @Override
    public void markAlerted(Long reportId) {
        ComplianceReportDO report = reportMapper.selectById(reportId);
        if (report == null) {
            throw exception(REPORT_NOT_EXISTS);
        }
        ComplianceReportDO update = new ComplianceReportDO();
        update.setId(reportId);
        update.setAlerted(true);
        reportMapper.updateById(update);
    }

    @Override
    public ComplianceReportDO getLatest(Long deviceId, String checkItem) {
        return reportMapper.selectLatestByDeviceAndItem(deviceId, checkItem);
    }
}

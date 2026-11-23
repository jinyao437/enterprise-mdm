package cn.enterprise.mdm.service.compliance;

import cn.enterprise.mdm.dal.dataobject.ComplianceReportDO;

import java.util.List;

/**
 * MDM 合规检查 Service
 */
public interface ComplianceService {

    /**
     * 接收设备 agent 的合规上报。不合规时自动打上待告警标记。
     *
     * @return 上报编号
     */
    Long report(ComplianceReportDO report);

    /** 查询未告警的不合规上报（供告警任务扫描推送 IM） */
    List<ComplianceReportDO> listUnalertedNonCompliant();

    /** 标记某条上报已告警 */
    void markAlerted(Long reportId);

    /** 查询设备某检查项的最新上报 */
    ComplianceReportDO getLatest(Long deviceId, String checkItem);
}

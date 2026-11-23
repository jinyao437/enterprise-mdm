package cn.enterprise.mdm.service.app;

import cn.enterprise.mdm.dal.dataobject.AppDistributionDO;
import cn.enterprise.mdm.dal.dataobject.AppDO;
import cn.enterprise.mdm.dal.dataobject.AppVersionDO;

import java.util.List;

/**
 * MDM 应用分发 Service（应用 + 版本 + 分发）
 */
public interface AppService {

    // ===== 应用 =====
    Long createApp(AppDO app);

    void updateApp(AppDO app);

    /** 上架/下架 */
    void updateAppStatus(Long id, Integer status);

    List<AppDO> getAppList();

    AppDO validateAppExists(Long id);

    // ===== 版本 =====
    Long addVersion(AppVersionDO version);

    List<AppVersionDO> getVersionList(Long appId);

    AppVersionDO validateVersionExists(Long id);

    // ===== 分发 =====
    /** 创建分发任务（含灰度比例校验） */
    Long createDistribution(AppDistributionDO distribution);

    /** 暂停分发 */
    void pauseDistribution(Long id);

    List<AppDistributionDO> listPendingDistributions();
}

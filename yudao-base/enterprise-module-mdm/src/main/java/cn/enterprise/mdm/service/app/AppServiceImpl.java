package cn.enterprise.mdm.service.app;

import cn.enterprise.mdm.dal.dataobject.AppDistributionDO;
import cn.enterprise.mdm.dal.dataobject.AppDO;
import cn.enterprise.mdm.dal.dataobject.AppVersionDO;
import cn.enterprise.mdm.dal.mysql.AppDistributionMapper;
import cn.enterprise.mdm.dal.mysql.AppMapper;
import cn.enterprise.mdm.dal.mysql.AppVersionMapper;
import cn.enterprise.mdm.enums.AppStatusEnum;
import cn.enterprise.mdm.enums.DistributionStatusEnum;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.enterprise.mdm.enums.ErrorCodeConstants.*;

/**
 * MDM 应用分发 Service 实现（租户隔离由底座处理，本类无租户代码）。
 */
@Service
@Validated
public class AppServiceImpl implements AppService {

    @Resource
    private AppMapper appMapper;
    @Resource
    private AppVersionMapper versionMapper;
    @Resource
    private AppDistributionMapper distributionMapper;

    // ===== 应用 =====
    @Override
    public Long createApp(AppDO app) {
        if (appMapper.selectByPackageName(app.getPackageName()) != null) {
            throw exception(APP_PACKAGE_DUPLICATE);
        }
        app.setId(null);
        if (app.getStatus() == null) {
            app.setStatus(AppStatusEnum.DRAFT.getStatus());
        }
        appMapper.insert(app);
        return app.getId();
    }

    @Override
    public void updateApp(AppDO app) {
        validateAppExists(app.getId());
        appMapper.updateById(app);
    }

    @Override
    public void updateAppStatus(Long id, Integer status) {
        validateAppExists(id);
        AppDO app = new AppDO();
        app.setId(id);
        app.setStatus(status);
        appMapper.updateById(app);
    }

    @Override
    public List<AppDO> getAppList() {
        return appMapper.selectList();
    }

    @Override
    public AppDO validateAppExists(Long id) {
        AppDO app = appMapper.selectById(id);
        if (app == null) {
            throw exception(APP_NOT_EXISTS);
        }
        return app;
    }

    // ===== 版本 =====
    @Override
    public Long addVersion(AppVersionDO version) {
        validateAppExists(version.getAppId());
        version.setId(null);
        versionMapper.insert(version);
        return version.getId();
    }

    @Override
    public List<AppVersionDO> getVersionList(Long appId) {
        return versionMapper.selectListByApp(appId);
    }

    @Override
    public AppVersionDO validateVersionExists(Long id) {
        AppVersionDO version = versionMapper.selectById(id);
        if (version == null) {
            throw exception(APP_VERSION_NOT_EXISTS);
        }
        return version;
    }

    // ===== 分发 =====
    @Override
    public Long createDistribution(AppDistributionDO distribution) {
        // 1. 校验应用存在且已上架
        AppDO app = validateAppExists(distribution.getAppId());
        if (AppStatusEnum.OFFLINE.getStatus().equals(app.getStatus())) {
            throw exception(APP_OFFLINE_CANNOT_DISTRIBUTE);
        }
        // 2. 校验版本存在
        validateVersionExists(distribution.getVersionId());
        // 3. 校验灰度比例 0-100
        Integer gray = distribution.getGrayPercent() == null ? 100 : distribution.getGrayPercent();
        if (gray < 0 || gray > 100) {
            throw exception(APP_GRAY_PERCENT_INVALID);
        }
        // 4. 落库（gray=100 直接全量，否则灰度）
        distribution.setId(null);
        distribution.setGrayPercent(gray);
        distribution.setStatus(gray == 100
                ? DistributionStatusEnum.PENDING.getStatus()
                : DistributionStatusEnum.GRAY.getStatus());
        distribution.setSuccessCount(0);
        distribution.setFailCount(0);
        distributionMapper.insert(distribution);
        return distribution.getId();
    }

    @Override
    public void pauseDistribution(Long id) {
        AppDistributionDO distribution = distributionMapper.selectById(id);
        if (distribution == null) {
            throw exception(APP_DISTRIBUTION_NOT_EXISTS);
        }
        AppDistributionDO update = new AppDistributionDO();
        update.setId(id);
        update.setStatus(DistributionStatusEnum.PAUSED.getStatus());
        distributionMapper.updateById(update);
    }

    @Override
    public List<AppDistributionDO> listPendingDistributions() {
        return distributionMapper.selectPending();
    }
}

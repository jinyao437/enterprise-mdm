package cn.enterprise.mdm.controller.admin.im;

import cn.enterprise.mdm.dal.dataobject.ImNotificationLogDO;
import cn.enterprise.mdm.service.im.ImNotificationService;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 管理后台 - MDM 业务 IM 通知（适配层）
 */
@Tag(name = "管理后台 - MDM IM 通知")
@RestController
@RequestMapping("/mdm/im/notification")
@Validated
public class ImNotificationController {

    @Resource
    private ImNotificationService notificationService;

    @PostMapping("/enqueue")
    @Operation(summary = "提交业务通知（幂等，进入本地消息表）")
    @PreAuthorize("@ss.hasPermission('mdm:im:notify')")
    public CommonResult<Long> enqueue(@RequestParam("eventId") String eventId,
                                      @RequestParam("messageType") Integer messageType,
                                      @RequestParam("recipient") Long recipient,
                                      @RequestParam("title") String title,
                                      @RequestParam("content") String content) {
        return success(notificationService.enqueue(eventId, messageType, recipient, title, content));
    }

    @GetMapping("/pending")
    @Operation(summary = "扫描待发送通知（供定时任务调用）")
    @PreAuthorize("@ss.hasPermission('mdm:im:query')")
    public CommonResult<List<ImNotificationLogDO>> listPending() {
        return success(notificationService.listPending());
    }

    @PostMapping("/try-send")
    @Operation(summary = "尝试发送一条通知")
    @PreAuthorize("@ss.hasPermission('mdm:im:notify')")
    public CommonResult<Boolean> trySend(@RequestParam("id") Long id) {
        return success(notificationService.trySend(id));
    }
}

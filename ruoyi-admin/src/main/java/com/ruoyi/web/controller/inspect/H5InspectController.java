package com.ruoyi.web.controller.inspect;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.domain.inspect.InsAddress;
import com.ruoyi.system.domain.inspect.InsLog;
import com.ruoyi.system.domain.inspect.InsLogPhoto;
import com.ruoyi.system.service.inspect.IInsAddressService;
import com.ruoyi.system.service.inspect.IInsCourtyardService;
import com.ruoyi.system.service.inspect.IInsLogService;

/**
 * H5微网格员接口
 * 使用 @PreAuthorize("@ss.hasRole('micro_grid')") 限制仅微网格员角色可访问
 */
@RestController
@RequestMapping("/h5")
public class H5InspectController extends BaseController
{
    @Autowired
    private IInsAddressService addressService;
    @Autowired
    private IInsLogService logService;
    @Autowired
    private IInsCourtyardService courtyardService;

    // ============ 地址搜索 ============

    /** H5地址自动补全（限定本院落，返回含预存租户信息） */
    @PreAuthorize("@ss.hasRole('micro_grid')")
    @GetMapping("/address/search")
    public AjaxResult searchAddress(@RequestParam String keyword)
    {
        Long courtyardId = getCurrentCourtyardId();
        List<InsAddress> list = addressService.searchAddress(keyword, courtyardId);
        return success(list);
    }

    // ============ 巡查日志 ============

    /** 提交巡查日志（含照片URL数组、租户信息） */
    @PreAuthorize("@ss.hasRole('micro_grid')")
    @PostMapping("/log")
    public AjaxResult submitLog(@RequestBody InsLog log)
    {
        Long courtyardId = getCurrentCourtyardId();
        log.setCourtyardId(courtyardId);
        log.setCreateBy(getUsername());
        log.setUpdateBy(getUsername());

        // 提取照片列表
        List<InsLogPhoto> photos = log.getPhotos();
        log.setPhotos(null);

        return toAjax(logService.insertLog(log, photos));
    }

    /** 微网格员编辑自己的日志 */
    @PreAuthorize("@ss.hasRole('micro_grid')")
    @PutMapping("/log")
    public AjaxResult editLog(@RequestBody InsLog log)
    {
        // 安全校验：只能编辑自己的日志
        InsLog exist = logService.selectLogById(log.getLogId());
        if (exist == null || !exist.getCreateBy().equals(getUsername())) {
            return error("无权修改此日志");
        }
        log.setUpdateBy(getUsername());
        List<InsLogPhoto> photos = log.getPhotos();
        log.setPhotos(null);
        return toAjax(logService.updateLogByWorker(log, photos));
    }

    /** 我的巡查日志列表（支持日期/地址筛选） */
    @PreAuthorize("@ss.hasRole('micro_grid')")
    @GetMapping("/log/myList")
    public TableDataInfo myLogList(InsLog query)
    {
        startPage();
        query.setCreateBy(getUsername());
        List<InsLog> list = logService.selectLogList(query);
        return getDataTable(list);
    }

    /** 日志详情 */
    @PreAuthorize("@ss.hasRole('micro_grid')")
    @GetMapping("/log/{logId}")
    public AjaxResult logDetail(@PathVariable Long logId)
    {
        InsLog log = logService.selectLogById(logId);
        if (log == null || !log.getCreateBy().equals(getUsername())) {
            return error("无权查看此日志");
        }
        return success(log);
    }

    // ============ 通用 ============

    /** 获取当前微网格员所属院落信息 */
    @PreAuthorize("@ss.hasRole('micro_grid')")
    @GetMapping("/courtyard/my")
    public AjaxResult myCourtyard()
    {
        Long courtyardId = getCurrentCourtyardId();
        return success(courtyardService.selectCourtyardById(courtyardId));
    }

    // ============ 私有方法 ============

    /** 从SecurityUtils获取当前用户的courtyard_id */
    private Long getCurrentCourtyardId()
    {
        return SecurityUtils.getLoginUser().getUser().getCourtyardId();
    }
}

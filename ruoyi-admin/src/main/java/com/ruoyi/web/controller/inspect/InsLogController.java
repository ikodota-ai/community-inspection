package com.ruoyi.web.controller.inspect;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.inspect.InsLog;
import com.ruoyi.system.service.inspect.IInsLogService;

/**
 * 巡查日志管理（PC端：总览、统计、订正、导出、租户档案）
 */
@RestController
@RequestMapping("/inspect/log")
public class InsLogController extends BaseController
{
    @Autowired
    private IInsLogService logService;

    /** 巡查日志列表（支持网格/类型/结果/时间筛选） */
    @PreAuthorize("@ss.hasPermi('inspect:dashboard:list')")
    @GetMapping("/list")
    public TableDataInfo list(InsLog log)
    {
        startPage();
        List<InsLog> list = logService.selectLogList(log);
        return getDataTable(list);
    }

    /** 日志详情（含照片） */
    @PreAuthorize("@ss.hasPermi('inspect:dashboard:list')")
    @GetMapping(value = "/{logId:\\d+}")
    public AjaxResult getInfo(@PathVariable Long logId)
    {
        return success(logService.selectLogById(logId));
    }

    /** 按网格统计（统计卡片数据） */
    @PreAuthorize("@ss.hasPermi('inspect:dashboard:list')")
    @GetMapping("/statistics")
    public AjaxResult statistics(InsLog log)
    {
        return success(logService.selectLogStatistics(log));
    }

    /** 特殊人员档案列表（支持按巡查小类筛选：tenant租户 solo独居） */
    @PreAuthorize("@ss.hasPermi('inspect:tenant:list')")
    @GetMapping("/tenantList")
    public TableDataInfo tenantList(InsLog log)
    {
        startPage();
        List<InsLog> list = logService.selectTenantList(log);
        return getDataTable(list);
    }

    /** 管理员订正日志 */
    @PreAuthorize("@ss.hasPermi('inspect:log:edit')")
    @Log(title = "巡查日志订正", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody InsLog log)
    {
        log.setUpdateBy(getUsername());
        return toAjax(logService.updateLogByAdmin(log));
    }

    /** 导出巡查日志 */
    @PreAuthorize("@ss.hasPermi('inspect:log:export')")
    @Log(title = "巡查日志导出", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, InsLog log)
    {
        List<InsLog> list = logService.selectLogList(log);
        ExcelUtil<InsLog> util = new ExcelUtil<InsLog>(InsLog.class);
        util.exportExcel(response, list, "巡查日志");
    }
}

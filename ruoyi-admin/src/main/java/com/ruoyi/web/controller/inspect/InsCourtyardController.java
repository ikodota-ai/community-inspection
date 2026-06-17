package com.ruoyi.web.controller.inspect;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.system.domain.inspect.InsCourtyard;
import com.ruoyi.system.service.inspect.IInsCourtyardService;

/**
 * 网格管理（PC端）
 */
@RestController
@RequestMapping("/inspect/courtyard")
public class InsCourtyardController extends BaseController
{
    @Autowired
    private IInsCourtyardService courtyardService;

    /** 网格列表（sidebar导航 + 筛选下拉） */
    @PreAuthorize("@ss.hasPermi('inspect:dashboard:list')")
    @GetMapping("/list")
    public AjaxResult list(InsCourtyard courtyard)
    {
        List<InsCourtyard> list = courtyardService.selectCourtyardList(courtyard);
        return success(list);
    }

    /** 全部正常网格 */
    @GetMapping("/all")
    public AjaxResult all()
    {
        return success(courtyardService.selectCourtyardAll());
    }

    @PreAuthorize("@ss.hasPermi('inspect:dashboard:list')")
    @GetMapping(value = "/{courtyardId}")
    public AjaxResult getInfo(@PathVariable Long courtyardId)
    {
        return success(courtyardService.selectCourtyardById(courtyardId));
    }

    @PreAuthorize("@ss.hasPermi('inspect:address:add')")
    @Log(title = "网格管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody InsCourtyard courtyard)
    {
        courtyard.setCreateBy(getUsername());
        return toAjax(courtyardService.insertCourtyard(courtyard));
    }

    @PreAuthorize("@ss.hasPermi('inspect:address:edit')")
    @Log(title = "网格管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody InsCourtyard courtyard)
    {
        courtyard.setUpdateBy(getUsername());
        return toAjax(courtyardService.updateCourtyard(courtyard));
    }

    @PreAuthorize("@ss.hasPermi('inspect:address:remove')")
    @Log(title = "网格管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{courtyardId}")
    public AjaxResult remove(@PathVariable Long courtyardId)
    {
        return toAjax(courtyardService.deleteCourtyardById(courtyardId));
    }
}

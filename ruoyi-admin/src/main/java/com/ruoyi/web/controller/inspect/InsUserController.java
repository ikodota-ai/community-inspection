package com.ruoyi.web.controller.inspect;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.system.service.ISysUserService;
import com.ruoyi.system.service.inspect.IInsCourtyardService;
import com.ruoyi.system.mapper.SysUserMapper;

/**
 * 人员网格分配（巡查管理专用）
 */
@RestController
@RequestMapping("/inspect/user")
public class InsUserController extends BaseController
{
    @Autowired
    private ISysUserService userService;

    /** 人员列表（含courtyard信息） */
    @PreAuthorize("@ss.hasPermi('inspect:dashboard:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysUser user)
    {
        startPage();
        List<SysUser> list = userService.selectUserList(user);
        return getDataTable(list);
    }

    /** 设置用户的courtyard_id */
    @PreAuthorize("@ss.hasPermi('inspect:log:edit')")
    @PutMapping("/courtyard")
    public AjaxResult setCourtyard(@RequestBody SysUser user)
    {
        if (user.getUserId() == null || user.getCourtyardId() == null) {
            return error("参数不完整");
        }
        SysUser update = new SysUser();
        update.setUserId(user.getUserId());
        update.setCourtyardId(user.getCourtyardId());
        userService.updateUser(update);
        return success();
    }
}

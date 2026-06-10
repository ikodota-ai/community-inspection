package com.ruoyi.web.controller.inspect;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.system.domain.inspect.InsAddress;
import com.ruoyi.system.service.inspect.IInsAddressService;

/**
 * 地址库管理（PC端）
 */
@RestController
@RequestMapping("/inspect/address")
public class InsAddressController extends BaseController
{
    @Autowired
    private IInsAddressService addressService;

    @PreAuthorize("@ss.hasPermi('inspect:address:list')")
    @GetMapping("/list")
    public TableDataInfo list(InsAddress address)
    {
        startPage();
        List<InsAddress> list = addressService.selectAddressList(address);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('inspect:address:query')")
    @GetMapping(value = "/{addressId}")
    public AjaxResult getInfo(@PathVariable Long addressId)
    {
        return success(addressService.selectAddressById(addressId));
    }

    @PreAuthorize("@ss.hasPermi('inspect:address:add')")
    @Log(title = "地址库管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody InsAddress address)
    {
        address.setCreateBy(getUsername());
        return toAjax(addressService.insertAddress(address));
    }

    @PreAuthorize("@ss.hasPermi('inspect:address:edit')")
    @Log(title = "地址库管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody InsAddress address)
    {
        address.setUpdateBy(getUsername());
        return toAjax(addressService.updateAddress(address));
    }

    @PreAuthorize("@ss.hasPermi('inspect:address:remove')")
    @Log(title = "地址库管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{addressIds}")
    public AjaxResult remove(@PathVariable Long[] addressIds)
    {
        return toAjax(addressService.deleteAddressById(addressIds[0]));
    }
}

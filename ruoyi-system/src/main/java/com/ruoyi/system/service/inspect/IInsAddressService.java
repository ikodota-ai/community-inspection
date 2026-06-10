package com.ruoyi.system.service.inspect;

import java.util.List;
import com.ruoyi.system.domain.inspect.InsAddress;

public interface IInsAddressService
{
    public List<InsAddress> selectAddressList(InsAddress address);
    public InsAddress selectAddressById(Long addressId);
    /** H5地址搜索：按院落+关键词模糊匹配，返回结果含预存租户信息 */
    public List<InsAddress> searchAddress(String keyword, Long courtyardId);
    public int insertAddress(InsAddress address);
    public int updateAddress(InsAddress address);
    public int deleteAddressById(Long addressId);
}

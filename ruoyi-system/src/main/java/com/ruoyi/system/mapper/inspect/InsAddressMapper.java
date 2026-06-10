package com.ruoyi.system.mapper.inspect;

import java.util.List;
import com.ruoyi.system.domain.inspect.InsAddress;

public interface InsAddressMapper
{
    public List<InsAddress> selectAddressList(InsAddress address);
    public InsAddress selectAddressById(Long addressId);
    public List<InsAddress> searchAddress(InsAddress address);
    public int insertAddress(InsAddress address);
    public int updateAddress(InsAddress address);
    public int deleteAddressById(Long addressId);
}

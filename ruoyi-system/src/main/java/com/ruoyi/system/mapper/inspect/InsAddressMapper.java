package com.ruoyi.system.mapper.inspect;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.inspect.InsAddress;

public interface InsAddressMapper
{
    public List<InsAddress> selectAddressList(InsAddress address);
    public InsAddress selectAddressById(Long addressId);
    public List<InsAddress> searchAddress(@Param("query") InsAddress address, @Param("subType") String subType);
    public int insertAddress(InsAddress address);
    public int updateAddress(InsAddress address);
    public int deleteAddressById(Long addressId);
}

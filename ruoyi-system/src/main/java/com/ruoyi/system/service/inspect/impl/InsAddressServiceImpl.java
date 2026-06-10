package com.ruoyi.system.service.inspect.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.domain.inspect.InsAddress;
import com.ruoyi.system.mapper.inspect.InsAddressMapper;
import com.ruoyi.system.service.inspect.IInsAddressService;

@Service
public class InsAddressServiceImpl implements IInsAddressService
{
    @Autowired
    private InsAddressMapper addressMapper;

    @Override
    public List<InsAddress> selectAddressList(InsAddress address) {
        return addressMapper.selectAddressList(address);
    }

    @Override
    public InsAddress selectAddressById(Long addressId) {
        return addressMapper.selectAddressById(addressId);
    }

    @Override
    public List<InsAddress> searchAddress(String keyword, Long courtyardId) {
        InsAddress query = new InsAddress();
        query.setAddressName(keyword);
        query.setCourtyardId(courtyardId);
        return addressMapper.searchAddress(query);
    }

    @Override
    public int insertAddress(InsAddress address) {
        return addressMapper.insertAddress(address);
    }

    @Override
    public int updateAddress(InsAddress address) {
        return addressMapper.updateAddress(address);
    }

    @Override
    public int deleteAddressById(Long addressId) {
        return addressMapper.deleteAddressById(addressId);
    }
}

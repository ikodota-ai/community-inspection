package com.ruoyi.system.service.inspect.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.domain.inspect.InsCourtyard;
import com.ruoyi.system.mapper.inspect.InsCourtyardMapper;
import com.ruoyi.system.service.inspect.IInsCourtyardService;

@Service
public class InsCourtyardServiceImpl implements IInsCourtyardService
{
    @Autowired
    private InsCourtyardMapper courtyardMapper;

    @Override
    public List<InsCourtyard> selectCourtyardAll() {
        return courtyardMapper.selectCourtyardAll();
    }

    @Override
    public InsCourtyard selectCourtyardById(Long courtyardId) {
        return courtyardMapper.selectCourtyardById(courtyardId);
    }

    @Override
    public List<InsCourtyard> selectCourtyardList(InsCourtyard courtyard) {
        return courtyardMapper.selectCourtyardList(courtyard);
    }

    @Override
    public int insertCourtyard(InsCourtyard courtyard) {
        return courtyardMapper.insertCourtyard(courtyard);
    }

    @Override
    public int updateCourtyard(InsCourtyard courtyard) {
        return courtyardMapper.updateCourtyard(courtyard);
    }

    @Override
    public int deleteCourtyardById(Long courtyardId) {
        return courtyardMapper.deleteCourtyardById(courtyardId);
    }
}

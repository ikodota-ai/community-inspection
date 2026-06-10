package com.ruoyi.system.service.inspect;

import java.util.List;
import com.ruoyi.system.domain.inspect.InsCourtyard;

public interface IInsCourtyardService
{
    public List<InsCourtyard> selectCourtyardAll();
    public InsCourtyard selectCourtyardById(Long courtyardId);
    public List<InsCourtyard> selectCourtyardList(InsCourtyard courtyard);
    public int insertCourtyard(InsCourtyard courtyard);
    public int updateCourtyard(InsCourtyard courtyard);
    public int deleteCourtyardById(Long courtyardId);
}

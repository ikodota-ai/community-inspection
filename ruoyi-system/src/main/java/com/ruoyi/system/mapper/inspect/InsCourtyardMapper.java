package com.ruoyi.system.mapper.inspect;

import java.util.List;
import com.ruoyi.system.domain.inspect.InsCourtyard;

public interface InsCourtyardMapper
{
    public List<InsCourtyard> selectCourtyardList(InsCourtyard courtyard);
    public List<InsCourtyard> selectCourtyardAll();
    public InsCourtyard selectCourtyardById(Long courtyardId);
    public int insertCourtyard(InsCourtyard courtyard);
    public int updateCourtyard(InsCourtyard courtyard);
    public int deleteCourtyardById(Long courtyardId);
}

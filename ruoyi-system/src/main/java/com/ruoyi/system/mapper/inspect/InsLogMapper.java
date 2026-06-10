package com.ruoyi.system.mapper.inspect;

import java.util.List;
import com.ruoyi.system.domain.inspect.InsLog;

public interface InsLogMapper
{
    public List<InsLog> selectLogList(InsLog log);
    public InsLog selectLogById(Long logId);
    public int insertLog(InsLog log);
    public int updateLog(InsLog log);
    public int deleteLogById(Long logId);
    /** 院落统计：按courtyard_id统计总量和隐患数 */
    public List<InsLog> selectLogStatistics(InsLog log);
    /** 租户档案：tenant_name非空的日志记录 */
    public List<InsLog> selectTenantList(InsLog log);
}

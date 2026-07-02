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
    /** 网格统计：按courtyard_id统计总量和隐患数 */
    public List<InsLog> selectLogStatistics(InsLog log);
    /** 租户档案：tenant_name非空的日志记录 */
    public List<InsLog> selectTenantList(InsLog log);
    /** 查询某个地址的所有历史租户列表（去重） */
    public List<InsLog> selectTenantListByAddressId(InsLog log);
    /** 查询某个地址的最新巡查记录 */
    public InsLog selectLatestLogByAddressId(Long addressId);
}

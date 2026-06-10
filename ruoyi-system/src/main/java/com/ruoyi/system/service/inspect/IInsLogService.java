package com.ruoyi.system.service.inspect;

import java.util.List;
import com.ruoyi.system.domain.inspect.InsLog;
import com.ruoyi.system.domain.inspect.InsLogPhoto;

public interface IInsLogService
{
    /** PC端：分页查询日志列表 */
    public List<InsLog> selectLogList(InsLog log);
    /** 日志详情（含照片） */
    public InsLog selectLogById(Long logId);
    /** H5/PC端：提交日志（含照片，事务） */
    public int insertLog(InsLog log, List<InsLogPhoto> photos);
    /** H5端：编辑自己的日志 */
    public int updateLogByWorker(InsLog log, List<InsLogPhoto> photos);
    /** PC端：管理员订正日志 */
    public int updateLogByAdmin(InsLog log);
    /** 统计：按院落汇总 */
    public List<InsLog> selectLogStatistics(InsLog log);
    /** 租户档案列表 */
    public List<InsLog> selectTenantList(InsLog log);
}

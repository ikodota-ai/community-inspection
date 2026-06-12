package com.ruoyi.system.domain.inspect;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 院落/网格单元
 * 
 * @author ruoyi
 */
public class InsCourtyard extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long courtyardId;
    private String courtyardName;
    private Long deptId;
    private String deptName;     // 网格名称（关联sys_dept）
    private Long gridDeptId;     // 父级网格dept_id
    private String gridName;     // 父级网格名称
    private Integer sortOrder;
    private String status;

    public Long getCourtyardId() { return courtyardId; }
    public void setCourtyardId(Long courtyardId) { this.courtyardId = courtyardId; }

    public String getCourtyardName() { return courtyardName; }
    public void setCourtyardName(String courtyardName) { this.courtyardName = courtyardName; }

    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }

    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }

    public Long getGridDeptId() { return gridDeptId; }
    public void setGridDeptId(Long gridDeptId) { this.gridDeptId = gridDeptId; }

    public String getGridName() { return gridName; }
    public void setGridName(String gridName) { this.gridName = gridName; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

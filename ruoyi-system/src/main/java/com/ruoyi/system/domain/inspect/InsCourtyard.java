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
    private Integer sortOrder;
    private String status;

    public Long getCourtyardId() { return courtyardId; }
    public void setCourtyardId(Long courtyardId) { this.courtyardId = courtyardId; }

    public String getCourtyardName() { return courtyardName; }
    public void setCourtyardName(String courtyardName) { this.courtyardName = courtyardName; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

package com.ruoyi.system.domain.inspect;

import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 巡查日志
 * 
 * @author ruoyi
 */
public class InsLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long logId;
    @Excel(name = "流水号", sort = 1)
    private String logCode;
    private Long courtyardId;

    @Excel(name = "所属院落", sort = 4)
    private String courtyardName;       // 冗余，关联查询用
    private Long addressId;

    @Excel(name = "巡查地址", sort = 5)
    private String addressName;

    @Excel(name = "巡查类型", dictType = "ins_place_type", sort = 7)
    private String mainType;

    @Excel(name = "巡查项目", dictType = "ins_inspect_item", sort = 8)
    private String subType;

    @Excel(name = "巡查结果", dictType = "ins_inspect_result", sort = 9)
    private String inspectResult;

    @Excel(name = "隐患级别", dictType = "ins_hazard_level", sort = 10)
    private String hazardLevel;

    @Excel(name = "问题描述", sort = 13, width = 30)
    private String description;

    @Excel(name = "特殊人员姓名", sort = 11)
    private String tenantName;

    @Excel(name = "特殊人员电话", sort = 12)
    private String tenantPhone;

    @Excel(name = "巡查时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss", sort = 2)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date inspectTime;

    @Excel(name = "巡查人", sort = 3)
    private String workerName;

    /** 巡查照片 */
    private List<InsLogPhoto> photos;

    /** 批量网格筛选（逗号分隔ID，非持久化） */
    private String courtyardIds;

    /** 统计用：网格汇总数据（非持久化字段） */
    private Integer totalCount;
    private Integer hazardCount;
    private Integer rectifiedCount;

    /** 前端搜索用：时间范围 */
    private Date beginTime;
    private Date endTime;

    public Long getLogId() { return logId; }
    public void setLogId(Long logId) { this.logId = logId; }

    public String getLogCode() { return logCode; }
    public void setLogCode(String logCode) { this.logCode = logCode; }

    public Long getCourtyardId() { return courtyardId; }
    public void setCourtyardId(Long courtyardId) { this.courtyardId = courtyardId; }

    public String getCourtyardName() { return courtyardName; }
    public void setCourtyardName(String courtyardName) { this.courtyardName = courtyardName; }

    public Long getAddressId() { return addressId; }
    public void setAddressId(Long addressId) { this.addressId = addressId; }

    public String getAddressName() { return addressName; }
    public void setAddressName(String addressName) { this.addressName = addressName; }

    public String getMainType() { return mainType; }
    public void setMainType(String mainType) { this.mainType = mainType; }

    public String getSubType() { return subType; }
    public void setSubType(String subType) { this.subType = subType; }

    public String getInspectResult() { return inspectResult; }
    public void setInspectResult(String inspectResult) { this.inspectResult = inspectResult; }

    public String getHazardLevel() { return hazardLevel; }
    public void setHazardLevel(String hazardLevel) { this.hazardLevel = hazardLevel; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTenantName() { return tenantName; }
    public void setTenantName(String tenantName) { this.tenantName = tenantName; }

    public String getTenantPhone() { return tenantPhone; }
    public void setTenantPhone(String tenantPhone) { this.tenantPhone = tenantPhone; }

    public Date getInspectTime() { return inspectTime; }
    public void setInspectTime(Date inspectTime) { this.inspectTime = inspectTime; }

    public String getWorkerName() { return workerName; }
    public void setWorkerName(String workerName) { this.workerName = workerName; }

    public List<InsLogPhoto> getPhotos() { return photos; }
    public void setPhotos(List<InsLogPhoto> photos) { this.photos = photos; }

    public Date getBeginTime() { return beginTime; }
    public void setBeginTime(Date beginTime) { this.beginTime = beginTime; }

    public Date getEndTime() { return endTime; }
    public void setEndTime(Date endTime) { this.endTime = endTime; }

    public Integer getTotalCount() { return totalCount; }
    public void setTotalCount(Integer totalCount) { this.totalCount = totalCount; }

    public Integer getHazardCount() { return hazardCount; }
    public void setHazardCount(Integer hazardCount) { this.hazardCount = hazardCount; }

    public Integer getRectifiedCount() { return rectifiedCount; }
    public void setRectifiedCount(Integer rectifiedCount) { this.rectifiedCount = rectifiedCount; }

    public String getCourtyardIds() { return courtyardIds; }
    public void setCourtyardIds(String courtyardIds) { this.courtyardIds = courtyardIds; }

    /** 详细地址（几单元几号） */
    @Excel(name = "详细地址", sort = 6)
    private String detailAddress;

    public String getDetailAddress() { return detailAddress; }
    public void setDetailAddress(String detailAddress) { this.detailAddress = detailAddress; }
}

package com.ruoyi.system.domain.inspect;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 巡查地址库
 * 
 * @author ruoyi
 */
public class InsAddress extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long addressId;
    private Long courtyardId;
    private String courtyardName;       // 冗余，关联查询用
    private String addressName;
    private String placeType;
    private String tenantName;
    private String tenantPhone;
    private String status;

    public Long getAddressId() { return addressId; }
    public void setAddressId(Long addressId) { this.addressId = addressId; }

    public Long getCourtyardId() { return courtyardId; }
    public void setCourtyardId(Long courtyardId) { this.courtyardId = courtyardId; }

    public String getCourtyardName() { return courtyardName; }
    public void setCourtyardName(String courtyardName) { this.courtyardName = courtyardName; }

    public String getAddressName() { return addressName; }
    public void setAddressName(String addressName) { this.addressName = addressName; }

    public String getPlaceType() { return placeType; }
    public void setPlaceType(String placeType) { this.placeType = placeType; }

    public String getTenantName() { return tenantName; }
    public void setTenantName(String tenantName) { this.tenantName = tenantName; }

    public String getTenantPhone() { return tenantPhone; }
    public void setTenantPhone(String tenantPhone) { this.tenantPhone = tenantPhone; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

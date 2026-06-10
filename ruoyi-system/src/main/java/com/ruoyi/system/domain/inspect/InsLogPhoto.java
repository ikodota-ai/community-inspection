package com.ruoyi.system.domain.inspect;

/**
 * 巡查照片
 * 
 * @author ruoyi
 */
public class InsLogPhoto
{
    private Long photoId;
    private Long logId;
    private String photoUrl;
    private Integer sortOrder;

    public Long getPhotoId() { return photoId; }
    public void setPhotoId(Long photoId) { this.photoId = photoId; }

    public Long getLogId() { return logId; }
    public void setLogId(Long logId) { this.logId = logId; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}

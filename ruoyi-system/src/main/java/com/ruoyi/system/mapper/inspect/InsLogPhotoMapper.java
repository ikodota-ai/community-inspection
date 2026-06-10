package com.ruoyi.system.mapper.inspect;

import java.util.List;
import com.ruoyi.system.domain.inspect.InsLogPhoto;

public interface InsLogPhotoMapper
{
    public List<InsLogPhoto> selectPhotosByLogId(Long logId);
    public int insertPhoto(InsLogPhoto photo);
    public int insertPhotos(List<InsLogPhoto> photos);
    public int deleteByLogId(Long logId);
}

package com.ruoyi.system.service.inspect.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.system.domain.inspect.InsLog;
import com.ruoyi.system.domain.inspect.InsLogPhoto;
import com.ruoyi.system.mapper.inspect.InsLogMapper;
import com.ruoyi.system.mapper.inspect.InsLogPhotoMapper;
import com.ruoyi.system.service.inspect.IInsLogService;

@Service
public class InsLogServiceImpl implements IInsLogService
{
    @Autowired
    private InsLogMapper logMapper;
    @Autowired
    private InsLogPhotoMapper photoMapper;

    @Override
    public List<InsLog> selectLogList(InsLog log) {
        return logMapper.selectLogList(log);
    }

    @Override
    public InsLog selectLogById(Long logId) {
        InsLog log = logMapper.selectLogById(logId);
        if (log != null) {
            log.setPhotos(photoMapper.selectPhotosByLogId(logId));
        }
        return log;
    }

    @Override
    @Transactional
    public int insertLog(InsLog log, List<InsLogPhoto> photos) {
        // 生成流水号: XJ-yyyyMMdd-NNN
        String dateStr = new SimpleDateFormat("yyyyMMdd").format(log.getInspectTime());
        log.setLogCode("XJ-" + dateStr + "-" + generateSeq());
        int rows = logMapper.insertLog(log);
        if (photos != null && !photos.isEmpty()) {
            for (InsLogPhoto p : photos) {
                p.setLogId(log.getLogId());
            }
            photoMapper.insertPhotos(photos);
        }
        return rows;
    }

    @Override
    @Transactional
    public int updateLogByWorker(InsLog log, List<InsLogPhoto> photos) {
        int rows = logMapper.updateLog(log);
        if (photos != null && !photos.isEmpty()) {
            photoMapper.deleteByLogId(log.getLogId());
            for (InsLogPhoto p : photos) {
                p.setLogId(log.getLogId());
            }
            photoMapper.insertPhotos(photos);
        }
        return rows;
    }

    @Override
    public int updateLogByAdmin(InsLog log) {
        return logMapper.updateLog(log);
    }

    @Override
    public List<InsLog> selectLogStatistics(InsLog log) {
        return logMapper.selectLogStatistics(log);
    }

    @Override
    public List<InsLog> selectTenantList(InsLog log) {
        return logMapper.selectTenantList(log);
    }

    /** 简单流水号自增（生产环境建议用Redis原子自增或号段表） */
    private synchronized String generateSeq() {
        return String.format("%03d", System.currentTimeMillis() % 1000);
    }
}

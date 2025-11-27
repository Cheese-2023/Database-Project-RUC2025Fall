package com.county.risk.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.county.risk.dto.DataQualityIssueDTO;
import com.county.risk.entity.DataQualityCheck;
import com.county.risk.mapper.DataQualityCheckMapper;
import com.county.risk.service.DataQualityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class DataQualityServiceImpl extends ServiceImpl<DataQualityCheckMapper, DataQualityCheck> implements DataQualityService {

    @Override
    public IPage<DataQualityIssueDTO> getCheckList(Page<DataQualityCheck> page) {
        return baseMapper.selectIssuePage(page);
    }

    @Override
    @Transactional
    public boolean resolveIssue(Integer id, String comment, Integer userId) {
        DataQualityCheck check = baseMapper.selectById(id);
        if (check != null) {
            check.setStatus("已解决");
            check.setResolutionComment(comment);
            check.setResolvedBy(userId);
            check.setResolvedAt(LocalDateTime.now());
            return baseMapper.updateById(check) > 0;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean ignoreIssue(Integer id, Integer userId) {
        DataQualityCheck check = baseMapper.selectById(id);
        if (check != null) {
            check.setStatus("已忽略");
            check.setResolvedBy(userId);
            check.setResolvedAt(LocalDateTime.now());
            return baseMapper.updateById(check) > 0;
        }
        return false;
    }
}

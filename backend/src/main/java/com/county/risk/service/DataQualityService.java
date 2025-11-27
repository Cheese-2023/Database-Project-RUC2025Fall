package com.county.risk.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.county.risk.dto.DataQualityIssueDTO;
import com.county.risk.entity.DataQualityCheck;

public interface DataQualityService extends IService<DataQualityCheck> {
    
    IPage<DataQualityIssueDTO> getCheckList(Page<DataQualityCheck> page);
    
    boolean resolveIssue(Integer id, String comment, Integer userId);
    
    boolean ignoreIssue(Integer id, Integer userId);
}

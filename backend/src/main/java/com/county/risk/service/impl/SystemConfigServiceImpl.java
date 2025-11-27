package com.county.risk.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.county.risk.entity.SystemConfig;
import com.county.risk.mapper.SystemConfigMapper;
import com.county.risk.service.SystemConfigService;
import org.springframework.stereotype.Service;

@Service
public class SystemConfigServiceImpl extends ServiceImpl<SystemConfigMapper, SystemConfig> implements SystemConfigService {
}

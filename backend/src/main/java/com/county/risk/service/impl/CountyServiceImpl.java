package com.county.risk.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.county.risk.entity.CountyBasic;
import com.county.risk.mapper.CountyBasicMapper;
import com.county.risk.service.CountyService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 县域信息服务实现
 */
@Service
public class CountyServiceImpl extends ServiceImpl<CountyBasicMapper, CountyBasic> implements CountyService {
    
    @Override
    public List<CountyBasic> getByProvince(String provinceName) {
        LambdaQueryWrapper<CountyBasic> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CountyBasic::getProvinceName, provinceName);
        return list(queryWrapper);
    }
}

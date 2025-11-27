package com.county.risk.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.county.risk.entity.CountyBasic;

import java.util.List;

/**
 * 县域信息服务接口
 */
public interface CountyService extends IService<CountyBasic> {
    /**
     * 根据省份查询县域列表
     */
    List<CountyBasic> getByProvince(String provinceName);
}

package com.xavier.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xavier.common.utils.PageUtils;
import com.xavier.mall.product.entity.SpuInfoDescEntity;

import java.util.Map;

/**
 * spu信息介绍
 *
 * @author Xavier
 * @email Xavier@gmail.com
 * @date 2022-11-01 20:34:50
 */
public interface SpuInfoDescService extends IService<SpuInfoDescEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSpuInfoDesc(SpuInfoDescEntity spuInfoDescEntity);
}


package com.xavier.mall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xavier.common.to.SkuReductionTo;
import com.xavier.common.utils.PageUtils;
import com.xavier.mall.coupon.entity.SkuFullReductionEntity;

import java.util.Map;

/**
 * 商品满减信息
 *
 * @author Xavier
 * @email Xavier@gmail.com
 * @date 2022-11-01 21:37:54
 */
public interface SkuFullReductionService extends IService<SkuFullReductionEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSkuReduction(SkuReductionTo skuReductionTo);
}


package com.xavier.mall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xavier.common.utils.PageUtils;
import com.xavier.mall.ware.entity.WareSkuEntity;

import java.util.Map;

/**
 * 商品库存
 *
 * @author Xavier
 * @email Xavier@gmail.com
 * @date 2022-11-01 21:49:41
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void addStock(Long skuId, Long wareId, Integer skuNum);
}


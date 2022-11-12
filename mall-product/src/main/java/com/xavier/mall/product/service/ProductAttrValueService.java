package com.xavier.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xavier.common.utils.PageUtils;
import com.xavier.mall.product.entity.ProductAttrValueEntity;

import java.util.List;
import java.util.Map;

/**
 * spu属性值
 *
 * @author Xavier
 * @email Xavier@gmail.com
 * @date 2022-11-01 20:34:50
 */
public interface ProductAttrValueService extends IService<ProductAttrValueEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveProductAttr(List<ProductAttrValueEntity> productAttrValueEntities);


    List<ProductAttrValueEntity> baseAttrListForSpu(Long spuId);

    void updateSpuAttr(Long spuId, List<ProductAttrValueEntity> entities);
}


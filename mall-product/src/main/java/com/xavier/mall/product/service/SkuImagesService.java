package com.xavier.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xavier.common.utils.PageUtils;
import com.xavier.mall.product.entity.SkuImagesEntity;

import java.util.List;
import java.util.Map;

/**
 * sku图片
 *
 * @author Xavier
 * @email Xavier@gmail.com
 * @date 2022-11-01 20:34:50
 */
public interface SkuImagesService extends IService<SkuImagesEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveKusImages(List<SkuImagesEntity> imagesEntities);


}


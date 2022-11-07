package com.xavier.mall.product.service.impl;

import com.xavier.mall.product.vo.SpuSaveVo;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xavier.common.utils.PageUtils;
import com.xavier.common.utils.Query;

import com.xavier.mall.product.dao.SpuInfoDao;
import com.xavier.mall.product.entity.SpuInfoEntity;
import com.xavier.mall.product.service.SpuInfoService;
import org.springframework.transaction.annotation.Transactional;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void saveSpuInfo(SpuSaveVo vo) {
        // 保存spu基本信息

        // 保存spu的描述图片

        // 保存spu的图片集

        // 保存spu的规格参数

        // spu的积分信息

        // 保存当前spu对应的所有sku信息
            // 1. sku的基本信息

            // 2. sku的图片信息

            // 3. sku的销售属性

            // 4. sku的优惠\满减信息
    }

}
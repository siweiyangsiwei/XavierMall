package com.xavier.mall.product.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.swing.clipboard.ImageSelection;
import com.xavier.common.to.SkuReductionTo;
import com.xavier.common.to.SpuBoundTo;
import com.xavier.common.utils.R;
import com.xavier.mall.product.dao.SpuInfoDescDao;
import com.xavier.mall.product.entity.*;
import com.xavier.mall.product.feign.CouponFeignService;
import com.xavier.mall.product.service.*;
import com.xavier.mall.product.vo.*;
import org.apache.commons.lang.StringUtils;
import org.aspectj.weaver.ast.Var;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xavier.common.utils.PageUtils;
import com.xavier.common.utils.Query;

import com.xavier.mall.product.dao.SpuInfoDao;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Resource
    private SpuInfoDao spuInfoDao;

    @Resource
    private SpuInfoDescService spuInfoDescService;

    @Resource
    private SpuImagesService spuImagesService;

    @Resource
    private ProductAttrValueService productAttrValueService;

    @Resource
    private AttrService attrService;

    @Resource
    private SkuInfoService skuInfoService;

    @Resource
    private SkuImagesService skuImagesService;

    @Resource
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @Resource
    private CouponFeignService couponFeignService;


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
        // 保存spu基本信息 pms_spu_info
        SpuInfoEntity spuInfoEntity = BeanUtil.copyProperties(vo, SpuInfoEntity.class);
        // todo 这里使用date可能出现线程不安全问题,看看后面有没有优化
        spuInfoEntity.setCreateTime(new Date());
        spuInfoEntity.setUpdateTime(new Date());
        this.saveBaseSpuInfo(spuInfoEntity);
        // 保存spu的描述图片 pms_spu_info_desc
        List<String> decript = vo.getDecript();
        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        spuInfoDescEntity.setSpuId(spuInfoEntity.getId());
        if (decript != null && !decript.isEmpty()) {
            spuInfoDescEntity.setDecript(String.join(",", decript));
            spuInfoDescService.saveSpuInfoDesc(spuInfoDescEntity);
        }
        // 保存spu的图片集 pms_spu_images
        List<String> images = vo.getImages();
        spuImagesService.saveImages(spuInfoEntity.getId(), images);
        // 保存spu的规格参数 pms_product_attr_value
        List<BaseAttrs> baseAttrs = vo.getBaseAttrs();
        List<ProductAttrValueEntity> productAttrValueEntities = baseAttrs.stream().map(attr -> {
            AttrEntity attrEntity = attrService.getById(attr.getAttrId());
            ProductAttrValueEntity productAttrValueEntity = new ProductAttrValueEntity();
            productAttrValueEntity.setAttrId(attr.getAttrId());
            productAttrValueEntity.setAttrValue(attr.getAttrValues());
            productAttrValueEntity.setSpuId(spuInfoEntity.getId());
            productAttrValueEntity.setAttrName(attrEntity.getAttrName());
            productAttrValueEntity.setQuickShow(attr.getShowDesc());
            return productAttrValueEntity;
        }).collect(Collectors.toList());
        productAttrValueService.saveProductAttr(productAttrValueEntities);
        // spu的积分信息 sms_spu_Bounds
        Bounds bounds = vo.getBounds();
        SpuBoundTo spuBoundTo = BeanUtil.copyProperties(bounds, SpuBoundTo.class);
        spuBoundTo.setSpuId(spuInfoEntity.getId());
        R r = couponFeignService.saveSpuBounds(spuBoundTo);
        if (r.getCode() != 0) {
            log.error("远程保存spu积分信息失败");
        }
        // 保存当前spu对应的所有sku信息
        List<Skus> skus = vo.getSkus();
        if (skus != null && !skus.isEmpty()) {
            // 1. sku的基本信息 pms_sku_info
            skus.forEach(item -> {
                String defaultImg = "";
                for (Images img : item.getImages()) {
                    if (img.getDefaultImg() == 1) {
                        defaultImg = img.getImgUrl();
                    }
                }
                SkuInfoEntity skuInfoEntity = BeanUtil.copyProperties(item, SkuInfoEntity.class);
                skuInfoEntity.setSpuId(spuInfoEntity.getId());
                skuInfoEntity.setBrandId(spuInfoEntity.getBrandId());
                skuInfoEntity.setCatalogId(spuInfoEntity.getCatalogId());
                skuInfoEntity.setSaleCount(0L);
                skuInfoEntity.setSkuDefaultImg(defaultImg);
                skuInfoService.saveSkuInfo(skuInfoEntity);
                Long skuId = skuInfoEntity.getSkuId();
                List<SkuImagesEntity> imagesEntities = item.getImages().stream().map((image -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setImgUrl(image.getImgUrl());
                    skuImagesEntity.setDefaultImg(image.getDefaultImg());
                    return skuImagesEntity;
                })).filter(image -> StringUtils.isNotEmpty(image.getImgUrl())) .collect(Collectors.toList());
                // 2. sku的图片信息 pms_sku_images
                skuImagesService.saveKusImages(imagesEntities);
                // 3. sku的销售属性 pms_sku_sale_attr_value
                List<Attr> attr = item.getAttr();
                List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = attr.stream().map(attr1 -> {
                    SkuSaleAttrValueEntity skuSaleAttrValueEntity = BeanUtil.copyProperties(attr1, SkuSaleAttrValueEntity.class);
                    skuSaleAttrValueEntity.setSkuId(skuInfoEntity.getSkuId());
                    return skuSaleAttrValueEntity;
                }).collect(Collectors.toList());
                skuSaleAttrValueService.saveSkuAttrValue(skuSaleAttrValueEntities);
                // 4. sku的优惠\满减信息
                SkuReductionTo skuReductionTo = BeanUtil.copyProperties(item, SkuReductionTo.class);
                skuReductionTo.setSkuId(skuInfoEntity.getSkuId());
                if (skuReductionTo.getFullCount() <= 0 || skuReductionTo.getFullPrice().compareTo(new BigDecimal("0")) == 1) {
                    R r1 = couponFeignService.saveSkuReduction(skuReductionTo);
                    if (r1.getCode() != 0) {
                        log.error("远程保存sku积分信息失败");
                    }
                }
            });

        }
    }

    @Override
    public void saveBaseSpuInfo(SpuInfoEntity spuInfoEntity) {
        spuInfoDao.insert(spuInfoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (StringUtils.isNotEmpty(key)){
            wrapper.and(w -> w.eq("id",key).or().like("spu_name",key));
        }

        String status = (String) params.get("status");
        if (StringUtils.isNotEmpty(status)){
            wrapper.eq("publish_status",status);
        }
        String brandId = (String) params.get("brandId");
        if (StringUtils.isNotEmpty(brandId) && !"0".equalsIgnoreCase(brandId)){
            wrapper.eq("brand_Id",brandId);
        }
        String catelogId = (String) params.get("catalogId");
        if (StringUtils.isNotEmpty(catelogId) && !"0".equalsIgnoreCase(catelogId)){
            wrapper.eq("catelog_id",catelogId);
        }

        IPage<SpuInfoEntity> page = this.page(new Query<SpuInfoEntity>().getPage(params), wrapper);
        return new PageUtils(page);
    }

}
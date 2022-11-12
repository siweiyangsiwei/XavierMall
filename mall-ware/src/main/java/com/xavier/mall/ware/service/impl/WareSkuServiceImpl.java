package com.xavier.mall.ware.service.impl;

import com.xavier.common.utils.R;
import com.xavier.mall.ware.feign.ProductFeignService;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xavier.common.utils.PageUtils;
import com.xavier.common.utils.Query;

import com.xavier.mall.ware.dao.WareSkuDao;
import com.xavier.mall.ware.entity.WareSkuEntity;
import com.xavier.mall.ware.service.WareSkuService;

import javax.annotation.Resource;
import javax.xml.crypto.Data;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Resource
    private WareSkuDao wareSkuDao;

    @Resource
    private ProductFeignService productFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareSkuEntity> wrapper = new QueryWrapper<>();
        String skuId = (String) params.get("skuId");
        if (StringUtils.isNotEmpty(skuId)){
            wrapper.eq("sku_id",skuId);
        }
        String wareId = (String) params.get("wareId");
        if (StringUtils.isNotEmpty(wareId)){
            wrapper.eq("ware_id",wareId);
        }
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }


    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        // 判断如果没有该库存,则是新增操作
        Integer count = wareSkuDao.selectCount(
                new QueryWrapper<WareSkuEntity>()
                        .eq("sku_id", skuId).eq("ware_id", wareId));
        if (count == null || count == 0){
            WareSkuEntity wareSkuEntity = new WareSkuEntity();
            wareSkuEntity.setSkuId(skuId);
            wareSkuEntity.setWareId(wareId);
            wareSkuEntity.setStock(skuNum);
            wareSkuEntity.setStockLocked(0);
            //远程查询sku的名字
            try {
                R info = productFeignService.info(skuId);
                if (info.getCode() == 0){
                    Map<String,Object> data = (Map<String, Object>) info.get("skuInfo");
                    String skuName = (String) data.get("skuName");
                    wareSkuEntity.setSkuName(skuName);
                }
            }catch (Exception e){

            }
            wareSkuDao.insert(wareSkuEntity);
        }
        wareSkuDao.addStock(skuId,wareId,skuNum);
    }

}
package com.xavier.mall.ware.service.impl;

import cn.hutool.core.io.file.PathUtil;
import com.sun.javafx.scene.shape.PathUtils;
import com.sun.org.apache.bcel.internal.generic.LOR;
import com.xavier.common.constant.WareConstant;
import com.xavier.mall.ware.controller.PurchaseController;
import com.xavier.mall.ware.entity.PurchaseDetailEntity;
import com.xavier.mall.ware.service.PurchaseDetailService;
import com.xavier.mall.ware.service.WareSkuService;
import com.xavier.mall.ware.vo.MergeVo;
import com.xavier.mall.ware.vo.PurchaseDoneVo;
import com.xavier.mall.ware.vo.PurchaseItemDoneVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.awt.dnd.DropTarget;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xavier.common.utils.PageUtils;
import com.xavier.common.utils.Query;

import com.xavier.mall.ware.dao.PurchaseDao;
import com.xavier.mall.ware.entity.PurchaseEntity;
import com.xavier.mall.ware.service.PurchaseService;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import static com.xavier.common.constant.WareConstant.PurchaseStatusEnum.*;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Resource
    private PurchaseDetailService purchaseDetailService;

    @Resource
    private WareSkuService wareSkuService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<PurchaseEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 0).or().eq("status", 1);
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageUnreceive(Map<String, Object> params) {

        QueryWrapper<PurchaseEntity> wrapper = new QueryWrapper<>();

        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void mergePurchase(MergeVo mergeVo) {
        Long purchaseId = mergeVo.getPurchaseId();
        if (purchaseId == null) {
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setCreateTime(new Date());
            purchaseEntity.setUpdateTime(new Date());
            purchaseEntity.setStatus(CREATED.getCode());
            this.save(purchaseEntity);
            purchaseId = purchaseEntity.getId();
        }

        // tode 确认采购单状态是0 或 1 才能确定
        List<Long> items = mergeVo.getItems();
        Long finalPurchaseId = purchaseId;
        List<PurchaseDetailEntity> collect = items.stream().map(i -> {
            PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
            purchaseDetailEntity.setPurchaseId(i);
            purchaseDetailEntity.setPurchaseId(finalPurchaseId);
            purchaseDetailEntity.setStatus(WareConstant.PurchaseSDetailStatusEnum.ASSIGNED.getCode());
            return purchaseDetailEntity;
        }).collect(Collectors.toList());
        purchaseDetailService.updateBatchById(collect);
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setUpdateTime(new Date());
        purchaseEntity.setId(purchaseId);
        this.updateById(purchaseEntity);
    }

    @Override
    public void received(List<Long> ids) {
        // 确认当前采购单是新建或已分配状态
        List<PurchaseEntity> purchaseEntities = ids.stream().map(id -> {
            PurchaseEntity entity = this.getById(id);
            return entity;
        }).filter(purchaseEntity -> {
            if (purchaseEntity.getStatus() == CREATED.getCode() || purchaseEntity.getStatus() == ASSIGNED.getCode()) {
                return true;
            }
            return false;
        }).map(item -> {
            item.setStatus(RECEIVE.getCode());
            item.setUpdateTime(new Date());
            return item;
        }).collect(Collectors.toList());
        // 改变采购单状态
        this.updateBatchById(purchaseEntities);
        // 改变采购项状态
        purchaseEntities.forEach(item -> {
            List<PurchaseDetailEntity> entities = purchaseDetailService.listDetailByPurchaseId(item.getId());
            entities.forEach(item1 -> item1.setStatus(WareConstant.PurchaseSDetailStatusEnum.BUYING.getCode()));
            purchaseDetailService.updateBatchById(entities);
        });
    }

    @Transactional
    @Override
    public void done(PurchaseDoneVo purchaseDoneVo) {
        Long id = purchaseDoneVo.getId();

        // 改变采购项的状态
        Boolean flag = true;
        List<PurchaseDetailEntity> updates = new ArrayList<>();
        for (PurchaseItemDoneVo item : purchaseDoneVo.getItems()) {
            PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
            if (item.getStatus() == HASERROR.getCode()){
                flag = false;
                log.warn("采购" + item.getItemId() + "失败,原因" + item.getReason());
                purchaseDetailEntity.setStatus(item.getStatus());
            }else{
                purchaseDetailEntity.setStatus(item.getStatus());
                // 将成功的采购进行入库
                PurchaseDetailEntity entity = purchaseDetailService.getById(item.getItemId());
                wareSkuService.addStock(entity.getSkuId(),entity.getWareId(),entity.getSkuNum());
            }
            purchaseDetailEntity.setId(item.getItemId());
            updates.add(purchaseDetailEntity);
        }
        purchaseDetailService.updateBatchById(updates);

        // 改变采购单的状态
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(id);
        purchaseEntity.setStatus(flag? FINISH.getCode() : HASERROR.getCode());
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);


    }

}
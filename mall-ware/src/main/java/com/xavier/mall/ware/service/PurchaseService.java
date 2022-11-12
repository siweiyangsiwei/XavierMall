package com.xavier.mall.ware.service;

import cn.hutool.core.io.file.PathUtil;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sun.javafx.scene.shape.PathUtils;
import com.xavier.common.utils.PageUtils;
import com.xavier.mall.ware.entity.PurchaseEntity;
import com.xavier.mall.ware.vo.MergeVo;
import com.xavier.mall.ware.vo.PurchaseDoneVo;

import java.util.List;
import java.util.Map;

/**
 * 采购信息
 *
 * @author Xavier
 * @email Xavier@gmail.com
 * @date 2022-11-01 21:49:41
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPageUnreceive(Map<String, Object> params);

    void mergePurchase(MergeVo mergeVo);

    void received(List<Long> ids);

    void done(PurchaseDoneVo purchaseDoneVo);

}


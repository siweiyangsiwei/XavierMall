package com.xavier.mall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xavier.common.utils.PageUtils;
import com.xavier.mall.ware.entity.PurchaseDetailEntity;

import java.util.List;
import java.util.Map;

/**
 * 
 *
 * @author Xavier
 * @email Xavier@gmail.com
 * @date 2022-11-01 21:49:41
 */
public interface PurchaseDetailService extends IService<PurchaseDetailEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<PurchaseDetailEntity> listDetailByPurchaseId(Long id);

}


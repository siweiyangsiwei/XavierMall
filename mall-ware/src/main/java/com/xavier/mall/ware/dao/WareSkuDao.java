package com.xavier.mall.ware.dao;

import com.xavier.mall.ware.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品库存
 * 
 * @author Xavier
 * @email Xavier@gmail.com
 * @date 2022-11-01 21:49:41
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {
	
}

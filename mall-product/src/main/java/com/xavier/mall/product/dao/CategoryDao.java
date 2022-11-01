package com.xavier.mall.product.dao;

import com.xavier.mall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author Xavier
 * @email Xavier@gmail.com
 * @date 2022-11-01 20:34:50
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}

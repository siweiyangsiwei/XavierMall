package com.xavier.mall.order.dao;

import com.xavier.mall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author Xavier
 * @email Xavier@gmail.com
 * @date 2022-11-01 21:47:43
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}

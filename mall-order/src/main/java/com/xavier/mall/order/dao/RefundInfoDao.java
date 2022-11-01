package com.xavier.mall.order.dao;

import com.xavier.mall.order.entity.RefundInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 退款信息
 * 
 * @author Xavier
 * @email Xavier@gmail.com
 * @date 2022-11-01 21:47:43
 */
@Mapper
public interface RefundInfoDao extends BaseMapper<RefundInfoEntity> {
	
}

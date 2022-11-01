package com.xavier.mall.coupon.dao;

import com.xavier.mall.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author Xavier
 * @email Xavier@gmail.com
 * @date 2022-11-01 21:37:55
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}

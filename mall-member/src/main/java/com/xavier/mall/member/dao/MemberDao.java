package com.xavier.mall.member.dao;

import com.xavier.mall.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author Xavier
 * @email Xavier@gmail.com
 * @date 2022-11-01 21:42:25
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}

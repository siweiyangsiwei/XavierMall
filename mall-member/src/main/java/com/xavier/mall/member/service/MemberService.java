package com.xavier.mall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xavier.common.utils.PageUtils;
import com.xavier.mall.member.entity.MemberEntity;

import java.util.Map;

/**
 * 会员
 *
 * @author Xavier
 * @email Xavier@gmail.com
 * @date 2022-11-01 21:42:25
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);
}


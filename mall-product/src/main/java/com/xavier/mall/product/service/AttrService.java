package com.xavier.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xavier.common.utils.PageUtils;
import com.xavier.mall.product.entity.AttrEntity;
import com.xavier.mall.product.vo.AttrGroupRelationVo;
import com.xavier.mall.product.vo.AttrRespVo;
import com.xavier.mall.product.vo.AttrVo;

import java.util.List;
import java.util.Map;

/**
 * 商品属性
 *
 * @author Xavier
 * @email Xavier@gmail.com
 * @date 2022-11-01 20:34:50
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveAttr(AttrVo attr);


    AttrRespVo getAttrInfo(Long attrId);

    void updateAttr(AttrVo attr);

    PageUtils queryAttrPage(Map<String, Object> params, Long catelogId, String type);

    List<Long> selectSearchAttrs(List<Long> attrIds);
}


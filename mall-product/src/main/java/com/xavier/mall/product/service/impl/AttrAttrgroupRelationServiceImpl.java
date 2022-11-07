package com.xavier.mall.product.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.xavier.mall.product.vo.AttrGroupRelationVo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xavier.common.utils.PageUtils;
import com.xavier.common.utils.Query;

import com.xavier.mall.product.dao.AttrAttrgroupRelationDao;
import com.xavier.mall.product.entity.AttrAttrgroupRelationEntity;
import com.xavier.mall.product.service.AttrAttrgroupRelationService;


@Service("attrAttrgroupRelationService")
public class AttrAttrgroupRelationServiceImpl extends ServiceImpl<AttrAttrgroupRelationDao, AttrAttrgroupRelationEntity> implements AttrAttrgroupRelationService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrAttrgroupRelationEntity> page = this.page(
                new Query<AttrAttrgroupRelationEntity>().getPage(params),
                new QueryWrapper<AttrAttrgroupRelationEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveBatch(List<AttrGroupRelationVo> vos) {
        List<AttrAttrgroupRelationEntity> attrAttrgroupRelationEntities = vos.stream()
                .map(item -> BeanUtil.copyProperties(item, AttrAttrgroupRelationEntity.class))
                .collect(Collectors.toList());
        this.saveBatch(attrAttrgroupRelationEntities);
    }

}
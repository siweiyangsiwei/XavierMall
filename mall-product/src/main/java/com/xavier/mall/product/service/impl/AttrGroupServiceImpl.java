package com.xavier.mall.product.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xavier.mall.product.controller.AttrGroupController;
import com.xavier.mall.product.dao.AttrAttrgroupRelationDao;
import com.xavier.mall.product.dao.AttrDao;
import com.xavier.mall.product.entity.AttrAttrgroupRelationEntity;
import com.xavier.mall.product.entity.AttrEntity;
import com.xavier.mall.product.service.AttrService;
import com.xavier.mall.product.vo.AttrGroupRelationVo;
import com.xavier.mall.product.vo.AttrGroupWithAttrsVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xavier.common.utils.PageUtils;
import com.xavier.common.utils.Query;

import com.xavier.mall.product.dao.AttrGroupDao;
import com.xavier.mall.product.entity.AttrGroupEntity;
import com.xavier.mall.product.service.AttrGroupService;
import org.w3c.dom.Attr;

import javax.annotation.Resource;

import static com.xavier.common.constant.ProductConstant.AttyEnum.ATTR_TYPE_BASE;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Resource
    private AttrGroupDao attrGroupDao;

    @Resource
    private AttrDao attrDao;

    @Resource
    private AttrAttrgroupRelationDao attrAttrgroupRelationDao;

    @Resource
    private AttrService attrService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils getAttrGroupByCatelogId(Map<String, Object> params, Long catelogId) {
        IPage<AttrGroupEntity> page = null;
        String key = (String) params.get("key");
        QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<AttrGroupEntity>();
        if (StringUtils.isNotEmpty(key)) {
            wrapper.and(obj -> {
                obj.eq("attr_group_id", key).or().like("attr_group_name", key);
            });
        }
        if (catelogId == 0) {
            page = this.page(new Query<AttrGroupEntity>().getPage(params), wrapper);
            return new PageUtils(page);
        }
        wrapper.eq("catelog_id", catelogId);
        page = this.page(new Query<AttrGroupEntity>().getPage(params), wrapper);

        return new PageUtils(page);
    }

    @Override
    public List<AttrEntity> getRelationAttr(Long attrgroupId) {
        List<AttrAttrgroupRelationEntity> relationList = attrAttrgroupRelationDao.selectList(
                new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", attrgroupId));
        List<Long> attrIds = relationList.stream().map(attr -> attr.getAttrId()).collect(Collectors.toList());
        if (!attrIds.isEmpty()) {
            return attrDao.selectList(new QueryWrapper<AttrEntity>().in("attr_id", attrIds));
        }
        return null;
    }

    @Override
    public void deleteRelation(AttrGroupRelationVo[] vos) {
        List<AttrAttrgroupRelationEntity> entities = Arrays.stream(vos)
                .map(item -> BeanUtil.copyProperties(item, AttrAttrgroupRelationEntity.class))
                .collect(Collectors.toList());
        attrAttrgroupRelationDao.deleteBatchRelation(entities);
    }

    /**
     * 获取当前分组没有关联的属性
     *
     * @param attrgroupId
     * @param params
     * @return
     */
    @Override
    public PageUtils getNoRelationAttr(Long attrgroupId, Map<String, Object> params) {
        AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrgroupId);
        if (attrGroupEntity != null) {
            Long catelogId = attrGroupEntity.getCatelogId();
            List<AttrGroupEntity> attrGroupEntities = attrGroupDao.selectList(
                    new QueryWrapper<AttrGroupEntity>()
                            .eq("catelog_id", catelogId));
            if (attrGroupEntities != null && BeanUtil.isNotEmpty(attrGroupEntities)) {
                List<Long> notInGroupIds = attrGroupEntities.stream().map(AttrGroupEntity::getAttrGroupId).collect(Collectors.toList());
                List<AttrAttrgroupRelationEntity> notInAttrRelations = attrAttrgroupRelationDao.selectList(
                        new QueryWrapper<AttrAttrgroupRelationEntity>()
                                .in("attr_group_id", notInGroupIds));
                QueryWrapper<AttrEntity> wrapper = new QueryWrapper<AttrEntity>()
                        .eq("catelog_id", catelogId)
                        .eq("attr_type", ATTR_TYPE_BASE.getCode());
                if (!notInAttrRelations.isEmpty()) {
                    List<Long> notInAttrIds = notInAttrRelations.stream()
                            .map(AttrAttrgroupRelationEntity::getAttrId)
                            .collect(Collectors.toList());
                    wrapper.notIn("attr_id", notInAttrIds);
                }
                String key = (String) params.get("key");
                if (StringUtils.isNotEmpty(key)) {
                    wrapper.and(w -> w.eq("attr_id", key).or().like("attr_name", key));
                }
                IPage<AttrEntity> page = attrService.page(new Query<AttrEntity>().getPage(params), wrapper);

                return new PageUtils(page);
            }
        }
        return null;
    }

    /**
     * 根据分类id查出所有的分组以及所有分组的属性
     *
     * @param catelogId
     * @return
     */
    @Override
    public List<AttrGroupWithAttrsVo> getAttrGroupWithAttrsByCatelogId(Long catelogId) {
        // 查询分组信息
        List<AttrGroupEntity> attrGroupEntities = this.list(
                new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));
        if (attrGroupEntities != null && !attrGroupEntities.isEmpty()) {
            // 查询所有属性
            List<AttrGroupWithAttrsVo> attrGroupWithAttrsVos = attrGroupEntities.stream().map(item -> {
                AttrGroupWithAttrsVo attrGroupWithAttrsVo = BeanUtil.copyProperties(item, AttrGroupWithAttrsVo.class);
                List<AttrEntity> attrEntities = this.getRelationAttr(item.getAttrGroupId());
                attrGroupWithAttrsVo.setAttrs(attrEntities);
                return attrGroupWithAttrsVo;
            }).collect(Collectors.toList());
            return attrGroupWithAttrsVos;
        }
        return null;
    }


}
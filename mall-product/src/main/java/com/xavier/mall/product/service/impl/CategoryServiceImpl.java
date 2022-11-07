package com.xavier.mall.product.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.json.JSONUtil;
import com.xavier.common.utils.CacheClient;
import com.xavier.common.utils.R;
import com.xavier.mall.product.service.CategoryBrandRelationService;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xavier.common.utils.PageUtils;
import com.xavier.common.utils.Query;

import com.xavier.mall.product.dao.CategoryDao;
import com.xavier.mall.product.entity.CategoryEntity;
import com.xavier.mall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import static com.xavier.common.utils.RedisConstants.XAVIERMALL_PRODUCT_CATEGORY_KEY;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Resource
    private CategoryDao categoryDao;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private CategoryBrandRelationService categoryBrandRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        String category = stringRedisTemplate.opsForValue().get(XAVIERMALL_PRODUCT_CATEGORY_KEY);
        List<CategoryEntity> entities = JSONUtil.toList(category, CategoryEntity.class);
        if (entities == null || entities.isEmpty()) {
            entities = categoryDao.selectList(null);
            stringRedisTemplate.opsForValue().set(XAVIERMALL_PRODUCT_CATEGORY_KEY, JSONUtil.toJsonStr(entities));
        }
        // 将所有的数据分类成一个3级分类
        return sortListWithTree(0L, entities);
    }

    @Override
    public void removeMenuByIds(List<Long> asList) {
        // TODO 检查当前伤处的菜单是否被别的地方引用

        categoryDao.deleteBatchIds(asList);

    }

    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> path = new ArrayList<>();
        findParentPath(catelogId, path);
        Collections.reverse(path);
        return ArrayUtil.toArray(path, Long.class);
    }

    /**
     * 级联更新所有关联的数据
     *
     * @param category
     */
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        if (StringUtils.isNotEmpty(category.getName())) {
            categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
            // todo 同步更新其他关联数据
        }
    }

    private void findParentPath(Long catelogId, List<Long> paths) {
        paths.add(catelogId);
        CategoryEntity byId = this.getById(catelogId);
        if (byId.getParentCid() != 0) {
            findParentPath(byId.getParentCid(), paths);
        }
    }


    private List<CategoryEntity> sortListWithTree(Long parentCid, List<CategoryEntity> entities) {
        List<CategoryEntity> listTree = entities.stream()
                .filter(categoryEntity -> categoryEntity.getParentCid().equals(parentCid))
                .map(menu -> {
                    menu.setChildren(sortListWithTree(menu.getCatId(), entities));
                    return menu;
                })
                .sorted(Comparator.comparingInt(menu -> (menu.getSort() == null ? 0 : menu.getSort())))
                .collect(Collectors.toList());
        return listTree;
    }

}
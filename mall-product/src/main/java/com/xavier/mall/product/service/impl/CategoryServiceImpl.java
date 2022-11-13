package com.xavier.mall.product.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.json.JSONUtil;
import com.xavier.common.utils.CacheClient;
import com.xavier.common.utils.R;
import com.xavier.mall.product.service.CategoryBrandRelationService;
import com.xavier.mall.product.vo.Catelog2Vo;
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

    @Override
    public List<CategoryEntity> getLevel1Categorys() {
        return baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
    }

    @Override
    public Map<String, List<Catelog2Vo>> getCatalogJson() {
        String catalogJson = stringRedisTemplate.opsForValue().get("catalogJson");
        if (catalogJson == null || StringUtils.isEmpty(catalogJson)) {
            Map<String, List<Catelog2Vo>> catalogJsonFromDB = getCatalogJsonFromDB();
            stringRedisTemplate.opsForValue().set("catalogJson",JSONUtil.toJsonStr(catalogJsonFromDB));
            return catalogJsonFromDB;
        }
        Map<String, List<Catelog2Vo>> catalog = JSONUtil.toBean(catalogJson, new TypeReference<Map<String, List<Catelog2Vo>>>() {
        }, false);
        return catalog;
    }


    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDB() {
        // 多次查询变为一次,查出所有
        List<CategoryEntity> selectList = baseMapper.selectList(null);

        List<CategoryEntity> level1Categorys = getCategoryEntities(selectList,0L);

        Map<String, List<Catelog2Vo>> collect = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            // value值的封装
            // 找出2级分类
            List<CategoryEntity> categoryEntities = getCategoryEntities(selectList,v.getCatId());
            // 进行2级分类封装
            List<Catelog2Vo> catelog2Vos = null;
            if (categoryEntities != null) {
                catelog2Vos = categoryEntities.stream().map(item -> {
                    Catelog2Vo catelog2Vo =
                            new Catelog2Vo(v.getCatId().toString(), null, item.getCatId().toString(), item.getName());
                    // 找出3级分类
                    List<CategoryEntity> categoryEntities1 = getCategoryEntities(selectList,item.getCatId());
                    // 3级分类封装
                    List<Catelog2Vo.Catelog3Vo> catelog3Vos = null;
                    if (categoryEntities1 != null) {
                        catelog3Vos = categoryEntities1.stream().map(item1 -> {
                            Catelog2Vo.Catelog3Vo catelog3Vo =
                                    new Catelog2Vo.Catelog3Vo(item.getCatId().toString(), item1.getCatId().toString(), item1.getName());
                            return catelog3Vo;
                        }).collect(Collectors.toList());
                        catelog2Vo.setCatalog3List(catelog3Vos);
                    }
                    return catelog2Vo;
                }).collect(Collectors.toList());
            }
            return catelog2Vos;
        }));
        return collect;
    }

    private List<CategoryEntity> getCategoryEntities( List<CategoryEntity> selectList,Long parentCid) {
        return selectList.stream().filter(item -> item.getParentCid() == parentCid).collect(Collectors.toList());
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
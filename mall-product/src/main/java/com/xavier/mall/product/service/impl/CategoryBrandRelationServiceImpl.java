package com.xavier.mall.product.service.impl;

import com.xavier.mall.product.dao.BrandDao;
import com.xavier.mall.product.dao.CategoryDao;
import com.xavier.mall.product.entity.BrandEntity;
import com.xavier.mall.product.service.BrandService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xavier.common.utils.PageUtils;
import com.xavier.common.utils.Query;

import com.xavier.mall.product.dao.CategoryBrandRelationDao;
import com.xavier.mall.product.entity.CategoryBrandRelationEntity;
import com.xavier.mall.product.service.CategoryBrandRelationService;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;


@Service("categoryBrandRelationService")
public class CategoryBrandRelationServiceImpl extends ServiceImpl<CategoryBrandRelationDao, CategoryBrandRelationEntity> implements CategoryBrandRelationService {

    @Resource
    private BrandDao brandDao;
    @Resource
    private CategoryDao categoryDao;
    @Resource
    private CategoryBrandRelationDao categoryBrandRelationDao;

    @Resource
    private BrandService brandService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryBrandRelationEntity> page = this.page(
                new Query<CategoryBrandRelationEntity>().getPage(params),
                new QueryWrapper<CategoryBrandRelationEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveDetail(CategoryBrandRelationEntity categoryBrandRelation) {
        Long brandId = categoryBrandRelation.getBrandId();
        Long catelogId = categoryBrandRelation.getCatelogId();
        // 查询详细名字
        String brandName = brandDao.selectById(brandId).getName();
        String catelogName = categoryDao.selectById(catelogId).getName();
        categoryBrandRelation.setBrandName(brandName);
        categoryBrandRelation.setCatelogName(catelogName);
        // 保存到数据库中
        this.save(categoryBrandRelation);
    }

    @Override
    public void updateBrand(Long brandId, String name) {
        CategoryBrandRelationEntity categoryBrandRelationEntity = new CategoryBrandRelationEntity();
        categoryBrandRelationEntity.setBrandId(brandId);
        categoryBrandRelationEntity.setBrandName(name);
        this.update(categoryBrandRelationEntity,
                new QueryWrapper<CategoryBrandRelationEntity>().eq("brand_id",brandId));
    }

    @Override
    public void updateCategory(Long catId, String name) {
        CategoryBrandRelationEntity categoryBrandRelationEntity = new CategoryBrandRelationEntity();
        categoryBrandRelationEntity.setCatelogId(catId);
        categoryBrandRelationEntity.setCatelogName(name);
        this.update(categoryBrandRelationEntity,
                new QueryWrapper<CategoryBrandRelationEntity>().eq("catelog_id",catId));
    }

    @Override
    public List<BrandEntity> getBrandsByCatId(Long catId) {
        List<CategoryBrandRelationEntity> entities = categoryBrandRelationDao.selectList(
                new QueryWrapper<CategoryBrandRelationEntity>()
                        .eq("catelog_id", catId));
        List<BrandEntity> brandEntities = entities.stream().map(item -> {
            Long brandId = item.getBrandId();
            return brandService.getById(brandId);
        }).collect(Collectors.toList());
        return brandEntities;
    }

}
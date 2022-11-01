package com.xavier.mall.product;

import com.xavier.mall.product.entity.BrandEntity;
import com.xavier.mall.product.service.BrandService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class MallProductApplicationTests {

    @Resource
    BrandService brandService;

    @Test
    void contextLoads() {
        BrandEntity brandEntity = new BrandEntity();
        brandEntity.setName("HUAWEI");
        brandService.save(brandEntity);
        System.out.println("成功");
    }

}

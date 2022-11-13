package com.xavier.mall.product;

import com.xavier.mall.product.entity.BrandEntity;
import com.xavier.mall.product.service.BrandService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

@SpringBootTest
class MallProductApplicationTests {

    @Resource
    BrandService brandService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Test
    void redisTest(){
        stringRedisTemplate.opsForValue().set("test:key","hello world");
    }

    @Test
    void contextLoads() {
        BrandEntity brandEntity = new BrandEntity();
        brandEntity.setName("HUAWEI");
        brandService.save(brandEntity);
        System.out.println("成功");
    }

}

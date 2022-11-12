package com.xavier.mall.ware.config;

import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement
@MapperScan("com.xavier.mall.ware.dao")
@Configuration
public class WareMyBatisConfig {
    @Bean
    public PaginationInterceptor paginationInterceptor(){
        PaginationInterceptor paginationInterceptor = new PaginationInterceptor();
        paginationInterceptor.setOverflow(true);
        paginationInterceptor.setLimit(1000);
        return paginationInterceptor;
    }
}

package com.xavier.mall.product.web;

import com.xavier.mall.product.entity.CategoryEntity;
import com.xavier.mall.product.service.CategoryService;
import com.xavier.mall.product.vo.Catelog2Vo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Controller
public class IndexController {

    @Resource
    private CategoryService categoryService;

    @GetMapping({"/", "/index.html"})
    public String indexPage(Model model) {
        // 查出所有的以及分类
        List<CategoryEntity> categoryEntities = categoryService.getLevel1Categorys();
        model.addAttribute("categorys", categoryEntities);
        return "index";
    }

    @ResponseBody
    @GetMapping("/index/catalog.json")
    public  Map<String, List<Catelog2Vo>> getCatalogJson() {
        return categoryService.getCatalogJson();
    }
}

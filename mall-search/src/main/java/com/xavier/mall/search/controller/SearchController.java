package com.xavier.mall.search.controller;

import com.xavier.mall.search.service.MallSearchService;
import com.xavier.mall.search.vo.SearchParam;
import com.xavier.mall.search.vo.SearchResp;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.annotation.Resource;

@Controller
public class SearchController {

    @Resource
    private MallSearchService mallSearchService;

    @GetMapping("/list.html")
    public String listPage(SearchParam searchParam, Model model){
        SearchResp result = mallSearchService.search(searchParam);
        model.addAttribute("result",result);
        return "list";
    }
}

package com.xavier.mall.search.service;

import com.xavier.mall.search.vo.SearchParam;
import com.xavier.mall.search.vo.SearchResp;

public interface MallSearchService {

    SearchResp search(SearchParam searchParam);
}

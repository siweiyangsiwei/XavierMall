package com.xavier.mall.search.service.impl;

import com.netflix.ribbon.proxy.annotation.Var;
import com.xavier.mall.search.service.MallSearchService;
import com.xavier.mall.search.vo.SearchParam;
import com.xavier.mall.search.vo.SearchResp;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.swing.*;

import java.io.IOException;

import static com.xavier.mall.search.config.ElasticSearchConfig.COMMON_OPTIONS;
import static com.xavier.mall.search.constant.EsConstant.PRODUCT_INDEX;
import static com.xavier.mall.search.constant.EsConstant.PRODUCT_PAGE_SIZE;

@Service
public class MallSearchServiceImpl implements MallSearchService {

    @Resource
    private RestHighLevelClient client;

    @Override
    public SearchResp search(SearchParam searchParam) {
        SearchResp result = null;

        SearchRequest searchRequest = buildSearchRequest(searchParam);

        SearchResponse response = null;
        try {
            response = client.search(searchRequest, COMMON_OPTIONS);
            result = buildSearchResult(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private SearchResp buildSearchResult(SearchResponse response) {
        return null;
    }

    /**
     * 准备检索请求
     *
     * @param param
     * @return
     */
    private SearchRequest buildSearchRequest(SearchParam param) {

        // 构建dsl语句
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 1. 构建bool - query
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        if (!StringUtils.isEmpty(param.getKeyword())) {
            // 1.1 must 模糊匹配
            boolQueryBuilder.must(QueryBuilders.matchQuery("skuTitle", param.getKeyword()));
        }
        // 1.2 filter

        // 1.2.1三场分类id查询条件
        if (param.getCatalog3Id() != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("catalogIg", param.getCatalog3Id()));
        }
        // 1.2.2 品牌id查询条件
        if (param.getBrandId() != null && param.getBrandId().size() > 0) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery("brandId", param.getBrandId()));
        }
        // 1.2.3 属性查询条件
        if (param.getAttrs() != null && param.getAttrs().size() > 0) {
            for (String attr : param.getAttrs()) {
                BoolQueryBuilder nesteBoolQuery = QueryBuilders.boolQuery();
                String[] s = attr.split("_");
                String attrId = s[0];
                String[] attrValues = s[1].split(":");
                nesteBoolQuery.must(QueryBuilders.termQuery("attrs.attrId", attrId));
                nesteBoolQuery.must(QueryBuilders.termsQuery("attrs.attrValue", attrValues));
                NestedQueryBuilder nestedQueryBuilder = QueryBuilders.nestedQuery("attrs", nesteBoolQuery, ScoreMode.None);

                boolQueryBuilder.filter(nestedQueryBuilder);
            }

        }

        // 1.2.4 库存查询条件
        boolQueryBuilder.filter(QueryBuilders.termQuery("hasStock", param.getHasStock() == 1));

        // 1.2.5 价格区间查询条件
        if (StringUtils.isEmpty(param.getSkuPrice())) {
            RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("skuPrice");
            String[] split = param.getSkuPrice().split("-");
            if (split.length == 2) {
                rangeQueryBuilder.gte(split[0]).lte(split[1]);
            } else if (split.length == 1) {
                if (param.getSkuPrice().startsWith("_")) {
                    rangeQueryBuilder.lte(split[0]);
                } else if (param.getSkuPrice().endsWith("_")) {
                    rangeQueryBuilder.gte(split[0]);
                }
            }
            boolQueryBuilder.filter(rangeQueryBuilder);
        }

        searchSourceBuilder.query(boolQueryBuilder);

        // 2.1 排序
        if (!StringUtils.isEmpty(param.getSort())){
            String sort = param.getSort();
            String[] s = sort.split("_");
            SortOrder order = s[1].equalsIgnoreCase("asc")? SortOrder.ASC:SortOrder.DESC;
            searchSourceBuilder.sort(s[0],order);
        }

        // 2.2 分页
        searchSourceBuilder.from((param.getPageNum() - 1) * PRODUCT_PAGE_SIZE);
        searchSourceBuilder.size(PRODUCT_PAGE_SIZE);

        // 2.3 高亮
        if (!StringUtils.isEmpty(param.getKeyword())){
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuTitle");
            highlightBuilder.preTags("<b style='color:red'>");
            highlightBuilder.postTags("</b>");
            searchSourceBuilder.highlighter(highlightBuilder);
        }

        // 3 聚合分析
        // 3.1 品牌聚合
        TermsAggregationBuilder brand_agg = AggregationBuilders.terms("brand_agg");
        brand_agg.field("brandId").size(50);
        brand_agg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(1));
        brand_agg.subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg").size(1));
        searchSourceBuilder.aggregation(brand_agg);

        // 3.2 分类聚合
        TermsAggregationBuilder catalog_agg = AggregationBuilders.terms("catalog_agg");
        catalog_agg.field("catalogId").size(50);
        catalog_agg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(1));
        searchSourceBuilder.aggregation(catalog_agg);

        // 3.3 属性聚合
        NestedAggregationBuilder attr_agg = AggregationBuilders.nested("attr_agg", "attrs");
        TermsAggregationBuilder attr_id_agg = AggregationBuilders.terms("attr_id.agg").field("attrs.attrId");
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1));
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(50));
        attr_agg.subAggregation(attr_id_agg);

        searchSourceBuilder.aggregation(attr_agg);

        SearchRequest searchRequest = new SearchRequest(new String[]{PRODUCT_INDEX}, searchSourceBuilder);
        return searchRequest;
    }
}

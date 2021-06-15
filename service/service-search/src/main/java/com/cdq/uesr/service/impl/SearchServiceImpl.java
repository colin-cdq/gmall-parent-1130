package com.cdq.uesr.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cdq.feign.ProductFeignClient11111;
import com.cdq.model.list.*;
import com.cdq.model.product.BaseCategoryView;
import com.cdq.repository.GoodsRepository;
import com.cdq.uesr.service.SearchService;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;


import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @program: gmall-parent-1130
 * @description:
 * @author: cdq
 * @create: 2021-05-30 23:25
 **/
@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    ProductFeignClient11111 productFeignClient;

    @Autowired
    ElasticsearchRestTemplate restTemplate;

    @Autowired
    GoodsRepository goodsRepository;

    @Autowired
    RestHighLevelClient restHighLevelClient;

    @Autowired
    RedisTemplate redisTemplate;

    @Override
    public List<JSONObject> getBaseCategoryList() {
        // 一级分类集合
        List<JSONObject> c1jsonObjects = new ArrayList<> ();

        // 从数据层(product)查询出分类数据
        List<BaseCategoryView> categoryViews = productFeignClient.getCategoryView ();

        // 对分类数据集合进行处理
        Map<Long, List<BaseCategoryView>> c1Map = categoryViews.stream ().collect ( Collectors.groupingBy ( BaseCategoryView::getCategory1Id ) );
        for (Map.Entry<Long, List<BaseCategoryView>> c1Object : c1Map.entrySet ()) {
            // 一级分类元素
            JSONObject c1jsonObject = new JSONObject ();
            Long c1Id = c1Object.getKey ();
            String category1Name = c1Object.getValue ().get ( 0 ).getCategory1Name ();
            c1jsonObject.put ( "categoryId" , c1Id );
            c1jsonObject.put ( "categoryName" , category1Name );
            // 二级分类集合
            List<JSONObject> c2jsonObjects = new ArrayList<> ();
            Map<Long, List<BaseCategoryView>> c2Map = c1Object.getValue ().stream ().collect ( Collectors.groupingBy ( BaseCategoryView::getCategory2Id ) );
            for (Map.Entry<Long, List<BaseCategoryView>> c2Object : c2Map.entrySet ()) {
                // 二级分类元素
                JSONObject c2jsonObject = new JSONObject ();
                Long c2Id = c2Object.getKey ();
                String category2Name = c2Object.getValue ().get ( 0 ).getCategory2Name ();
                c2jsonObject.put ( "categoryId" , c2Id );
                c2jsonObject.put ( "categoryName" , category2Name );
                // 三级分类集合
                List<JSONObject> c3jsonObjects = new ArrayList<> ();
                Map<Long, List<BaseCategoryView>> c3Map = c2Object.getValue ().stream ().collect ( Collectors.groupingBy ( BaseCategoryView::getCategory3Id ) );
                for (Map.Entry<Long, List<BaseCategoryView>> c3Object : c3Map.entrySet ()) {
                    // 三级分类元素
                    JSONObject c3jsonObject = new JSONObject ();
                    Long c3Id = c3Object.getKey ();
                    String category3Name = c3Object.getValue ().get ( 0 ).getCategory3Name ();
                    c3jsonObject.put ( "categoryId" , c3Id );
                    c3jsonObject.put ( "categoryName" , category3Name );
                    c3jsonObjects.add ( c3jsonObject );
                }
                c2jsonObject.put ( "categoryChild" , c3jsonObjects );
                c2jsonObjects.add ( c2jsonObject );
            }
            c1jsonObject.put ( "categoryChild" , c2jsonObjects );
            c1jsonObjects.add ( c1jsonObject );
        }

        return c1jsonObjects;
    }

    @Override
    public void cancelSale(Long skuId) {
        // 根据下架的商品skuId，将数据从es中删除(genjuid删除)
        //goods是表的实现类
        Goods goods = new Goods ();
        goods.setId ( skuId );
        goodsRepository.delete ( goods );
    }

    @Override
    public void onSale(Long skuId) {
        // 根据上架的商品skuId，将数据放入es中

        // skuInfo，attrs，tm
        Goods goods = productFeignClient.getGoodsBySkuId ( skuId );
        goods.setCreateTime ( new Date () );
        goodsRepository.save ( goods );
    }

    @Override
    public void createIndex(String index , String type) {
        //        Class<? extends String> indexClass = index.getClass();
//        Class<? extends String> typeClass = type.getClass();

        Class<?> indexClass = null;

        try {
            indexClass = Class.forName ( index );
        } catch (ClassNotFoundException e) {
            e.printStackTrace ();
        }
        restTemplate.createIndex ( indexClass );// 创建索引
        restTemplate.putMapping ( indexClass );// 创建数据结构
    }

    @Override
    public SearchResponseVo list(SearchParam searchParam) {

        //调用封装的查询语句方法getSearchRequest
        SearchRequest searchRequest = getSearchRequest(searchParam);

        //封装的返回结果
        SearchResponse searchResponse = null;
        try {
            //返回结果
            searchResponse = restHighLevelClient.search ( searchRequest , RequestOptions.DEFAULT );
        } catch (IOException e) {
            e.printStackTrace ();
        }

        //调用解析结果方法getSearchResponseVo
        SearchResponseVo searchResponseVo = getSearchResponseVo ( searchResponse );

        return searchResponseVo;
    }

    @Override
    public void hotScore(Long skuId) {
        // 先缓存
        Long increment = redisTemplate.opsForValue().increment("sku:" + skuId + ":hotScore", 1);

        if(increment%10==0){
            // 再es
            Optional<Goods> optional = goodsRepository.findById(skuId);
            Goods goods = optional.get();
            goods.setHotScore(increment);
            goodsRepository.save(goods);
        }
    }

    SearchRequest getSearchRequest(SearchParam searchParam) {
        //封装的查询语句

        // 执行检索语句,使用es的api:restHighLevelClient
        SearchRequest searchRequest = new SearchRequest();
        //搜索索引的范围
        searchRequest.indices("goods");
        //搜索表的范围
        searchRequest.types("info");

        // dsl语句封装
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder ();
        searchSourceBuilder.size (  60);
        searchSourceBuilder.from (0);

        // 与keyword不能同时为空
        Long category3Id = searchParam.getCategory3Id ();
        // 与category3Id不能同时为空
        String keyword = searchParam.getKeyword ();
        //org.apache.lucene.util的QueryBuilder
        //QueryBuilder queryBuilder = new QueryBuilder ();
        // 属性数组
        String[] props = searchParam.getProps();
        // 品牌
        String trademark = searchParam.getTrademark();
        String order = searchParam.getOrder();

        // query下的复合搜索的dsl封装
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        //判断category3Id、keyword
        if (!StringUtils.isEmpty ( category3Id )){
            // 封装分类
            TermQueryBuilder termQueryBuilder = new TermQueryBuilder ("category3Id", category3Id);
            boolQueryBuilder.filter(termQueryBuilder);
        }

        if (!StringUtils.isEmpty ( keyword )){
            // 封装关键字
            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("title", keyword);
            boolQueryBuilder.must(matchQueryBuilder);
        }


        if (null != props && props.length > 0) {
            for (String prop : props) {
                String[] split = prop.split(":");
                String attrId = split[0];
                String attrVaule = split[1];
                String attrName = split[2];

                BoolQueryBuilder nestedBoolQueryBuilder = new BoolQueryBuilder();

                TermQueryBuilder attrIdTermQueryBuilder = new TermQueryBuilder("attrs.attrId", attrId);
                nestedBoolQueryBuilder.filter(attrIdTermQueryBuilder);
                TermQueryBuilder attrNameTermQueryBuilder = new TermQueryBuilder("attrs.attrName", attrName);
                nestedBoolQueryBuilder.filter(attrNameTermQueryBuilder);
                TermQueryBuilder attrValueTermQueryBuilder = new TermQueryBuilder("attrs.attrValue", attrVaule);
                nestedBoolQueryBuilder.filter(attrValueTermQueryBuilder);

                NestedQueryBuilder nestedQueryBuilder = new NestedQueryBuilder("attrs",nestedBoolQueryBuilder, ScoreMode.None);
                boolQueryBuilder.filter(nestedQueryBuilder);
            }
        }

        if(!StringUtils.isEmpty(trademark)){
            String[] split = trademark.split(":");
            String tmId = split[0];
            String tmName = split[1];

            TermQueryBuilder tmIdTermQueryBuilder = new TermQueryBuilder("tmId", tmId);
            boolQueryBuilder.filter(tmIdTermQueryBuilder);

            TermQueryBuilder tmNameTermQueryBuilder = new TermQueryBuilder("tmName", tmName);
            boolQueryBuilder.filter(tmNameTermQueryBuilder);
        }

        searchSourceBuilder.query(boolQueryBuilder);

        // 商标聚合
        TermsAggregationBuilder tmTermsAggregationBuilder = AggregationBuilders.terms("tmIdAgg").field("tmId");
        searchSourceBuilder.aggregation(tmTermsAggregationBuilder
                .subAggregation(AggregationBuilders.terms("tmNameAgg").field("tmName"))
                .subAggregation(AggregationBuilders.terms("tmLogoUrlAgg").field("tmLogoUrl")));


        // 属性聚合
        NestedAggregationBuilder attrsNestedAggregationBuilder = AggregationBuilders.nested("attrsAgg", "attrs");
        searchSourceBuilder.aggregation(AggregationBuilders.nested("attrsAgg", "attrs")
                .subAggregation(AggregationBuilders.terms("attrIdAgg").field("attrs.attrId")
                .subAggregation(AggregationBuilders.terms("attrValueAgg").field("attrs.attrValue"))
                .subAggregation(AggregationBuilders.terms("attrNameAgg").field("attrs.attrName"))));


        // 排序
        if(StringUtils.isEmpty(order)){
            searchSourceBuilder.sort("hotScore", SortOrder.DESC);
        }else {
            String[] split = order.split(":");
            String orderNum = split[0];//1 热度 2 价格
            String orderSort = split[1];// asc/desc
            if(orderNum.equals("1")){
                searchSourceBuilder.sort("hotScore",orderSort.equals("desc")?SortOrder.DESC:SortOrder.ASC);
            }else if(orderNum.equals("2")){
                searchSourceBuilder.sort("price",orderSort.equals("desc")?SortOrder.DESC:SortOrder.ASC);
            }
        }






        //搜索语句
        searchRequest.source(searchSourceBuilder);

        return searchRequest;
    }


    SearchResponseVo getSearchResponseVo(SearchResponse searchResponse) {
        SearchResponseVo searchResponseVo = new SearchResponseVo ();
        //判断返回结果
        if (null != searchResponse && searchResponse.getHits ().totalHits > 0) {

            List<Goods> listGoods = new ArrayList<> ();

            SearchHit[] hits = searchResponse.getHits ().getHits ();
            //遍历结果数据
            for (SearchHit hit : hits) {
                String sourceAsString = hit.getSourceAsString ();
                Goods goods = JSON.parseObject ( sourceAsString , Goods.class );

                listGoods.add ( goods );
            }
            // 放入商品列表
            searchResponseVo.setGoodsList ( listGoods );
            Aggregations aggregations = searchResponse.getAggregations();

            if (null != aggregations) {
                ParsedLongTerms tmIdAgg = (ParsedLongTerms) aggregations.get("tmIdAgg");
                List<SearchResponseTmVo> searchResponseTmVos = tmIdAgg.getBuckets().stream().map(tmBucket -> {
                    SearchResponseTmVo searchResponseTmVo = new SearchResponseTmVo();
                    // 商标id
                    long tmId = tmBucket.getKeyAsNumber().longValue();
                    // 商标名称
                    ParsedStringTerms tmNameAgg = (ParsedStringTerms) tmBucket.getAggregations().get("tmNameAgg");
                    String tmName = tmNameAgg.getBuckets().get(0).getKeyAsString();
                    // 商标logo
                    ParsedStringTerms tmLogoUrlAgg = (ParsedStringTerms) tmBucket.getAggregations().get("tmLogoUrlAgg");
                    String tmLogoUrl = tmLogoUrlAgg.getBuckets().get(0).getKeyAsString();

                    searchResponseTmVo.setTmId(tmId);
                    searchResponseTmVo.setTmName(tmName);
                    searchResponseTmVo.setTmLogoUrl(tmLogoUrl);
                    return searchResponseTmVo;
                }).collect(Collectors.toList());
                // 放入商标列表
                searchResponseVo.setTrademarkList(searchResponseTmVos);
            }

            // 属性聚合解析
            ParsedNested attrsAgg = (ParsedNested) aggregations.get("attrsAgg");
            ParsedLongTerms attrIdAgg = (ParsedLongTerms) attrsAgg.getAggregations().get("attrIdAgg");

            List<SearchResponseAttrVo> searchResponseAttrVos = attrIdAgg.getBuckets().stream().map( attrIdBucket -> {
                long attrId = attrIdBucket.getKeyAsNumber().longValue();
                ParsedStringTerms attrNameParsedStringTerms = (ParsedStringTerms) attrIdBucket.getAggregations().get("attrNameAgg");
                String attrName = attrNameParsedStringTerms.getBuckets().get(0).getKeyAsString();

                SearchResponseAttrVo searchResponseAttrVo = new SearchResponseAttrVo();
                searchResponseAttrVo.setAttrId(attrId);
                searchResponseAttrVo.setAttrName(attrName);

                ParsedStringTerms attrValueParsedStringTerms = (ParsedStringTerms) attrIdBucket.getAggregations().get("attrValueAgg");
                List<String> attrValues = attrValueParsedStringTerms.getBuckets().stream().map(attrValueBucket -> {
                    String attrValue = attrValueBucket.getKeyAsString();
                    return attrValue;
                }).collect(Collectors.toList());
                searchResponseAttrVo.setAttrValueList(attrValues);
                return searchResponseAttrVo;
            }).collect(Collectors.toList());
            searchResponseVo.setAttrsList(searchResponseAttrVos);// 放入属性列表
        }

        return  searchResponseVo;
    }





}

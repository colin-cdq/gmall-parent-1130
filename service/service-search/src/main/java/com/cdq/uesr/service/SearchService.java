package com.cdq.uesr.service;

import com.alibaba.fastjson.JSONObject;
import com.cdq.model.list.SearchParam;
import com.cdq.model.list.SearchResponseVo;

import java.util.List;

/**
 * @program: gmall-parent-1130
 * @description:
 * @author: cdq
 * @create: 2021-05-30 23:24
 **/
public interface SearchService {
    List<JSONObject> getBaseCategoryList();

    void cancelSale(Long skuId);

    void onSale(Long skuId);

    void createIndex(String index , String type);

    SearchResponseVo list(SearchParam searchParam);

    void hotScore(Long skuId);
}

package com.cdq.uesr.controller;

import com.alibaba.fastjson.JSONObject;
import com.cdq.common.util.Result;
import com.cdq.model.list.SearchParam;
import com.cdq.model.list.SearchResponseVo;
import com.cdq.uesr.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @program: gmall-parent-1130
 * @description:
 * @author: cdq
 * @create: 2021-05-30 23:24
 **/
@RestController
@RequestMapping("api/search")
public class SearchApiController {

    @Autowired
    SearchService searchService;

    @RequestMapping("index")
    List<JSONObject> index(){
        List<JSONObject> jsonObjects = searchService.getBaseCategoryList();

        return jsonObjects;
    }

    @RequestMapping("cancelSale/{skuId}")
    void cancelSale(@PathVariable("skuId") Long skuId){
        searchService.cancelSale(skuId);
    }

    @RequestMapping("onSale/{skuId}")
    void onSale(@PathVariable("skuId") Long skuId){
        searchService.onSale(skuId);
    }

    @RequestMapping("create/{index}/{type}")
    Result create(@PathVariable("index") String index,@PathVariable("type") String type){
        searchService.createIndex(index,type);

        return Result.ok();
    }

    @RequestMapping("list")
    SearchResponseVo list(@RequestBody SearchParam searchParam){
        SearchResponseVo responseVo = searchService.list(searchParam);

        return responseVo;
    }

    @RequestMapping("hotScore/{skuId}")
    void hotScore(@PathVariable("skuId") Long skuId){
        searchService.hotScore(skuId);
    }



}

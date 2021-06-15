package com.cdq.uesr.controller;

import com.alibaba.fastjson.JSONObject;
import com.cdq.model.list.Goods;
import com.cdq.model.list.SearchAttr;
import com.cdq.model.list.SearchParam;
import com.cdq.model.list.SearchResponseVo;
import com.cdq.search.client.SearchFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @program: gmall-parent-1130
 * @description:
 * @author: cdq
 * @create: 2021-05-30 20:01
 **/
@Controller
public class SearchController {

   @Autowired
    SearchFeignClient searchFeignClient;

    @RequestMapping({"list.html","search.html"})
    public String list(Model model, SearchParam searchParam, HttpServletRequest request){
        StringBuffer requestURL = request.getRequestURL();
        SearchResponseVo searchResponseVo = searchFeignClient.list(searchParam);

        List<Goods> goodsList = searchResponseVo.getGoodsList();
        model.addAttribute("goodsList",goodsList);
        model.addAttribute("trademarkList",searchResponseVo.getTrademarkList());
        model.addAttribute("attrsList", searchResponseVo.getAttrsList());
        model.addAttribute("urlParam",  requestURL + "?" + getUrlParam(searchParam));// 将当前的url传入前台
       //有品牌时才传递
        if(!StringUtils.isEmpty(searchParam.getTrademark())){
            model.addAttribute("trademarkParam",searchParam.getTrademark().split(":")[1]);
        }
        //属性面包屑
        if(null!=searchParam.getProps()&&searchParam.getProps().length>0){
            List<SearchAttr> propsParamList = new ArrayList<> ();
            for (String prop : searchParam.getProps()) {
                String[] split = prop.split(":");
                String attrId = split[0];
                String attrVaule = split[1];
                String attrName = split[2];
                SearchAttr searchAttr = new SearchAttr();

                searchAttr.setAttrId(Long.parseLong(attrId));
                searchAttr.setAttrName(attrName);
                searchAttr.setAttrValue(attrVaule);

                propsParamList.add(searchAttr);
            }
            model.addAttribute("propsParamList",propsParamList);

        }
        if(!StringUtils.isEmpty(searchParam.getOrder())){
            Map<String,String> orderMap = new HashMap<> ();
            String[] split = searchParam.getOrder().split(":");
            String orderNum = split[0];//1 热度 2 价格
            String orderSort = split[1];// asc/desc
            orderMap.put("type",orderNum);
            orderMap.put("sort",orderSort);
            model.addAttribute("orderMap",orderMap);
        }
        return "list/index";
    }


    private Object getUrlParam(SearchParam searchParam) {
        String urlParam = "";

        String keyword = searchParam.getKeyword();
        Long category3Id = searchParam.getCategory3Id();

        //品牌参数的过滤
        String[] props = searchParam.getProps();
        String trademark = searchParam.getTrademark();

        if (!StringUtils.isEmpty(keyword)) {
            urlParam = urlParam + "keyword=" + keyword;
        }

        if (!StringUtils.isEmpty(category3Id)) {
            urlParam = urlParam + "category3Id=" + category3Id;
        }

        //品牌参数的过滤
        if (null != props && props.length > 0) {
            for (String prop : props) {
//                String[] split = prop.split(":");
//                String attrId = split[0];
//                String attrVaule = split[1];
//                String attrName = split[2];
//
                urlParam = urlParam + "&props=" + prop;
            }
        }

        return urlParam;
    }




    @RequestMapping({"index.html","/"})
    public String index(Model model){
        List<JSONObject> jsonObjects = searchFeignClient.index();
        //list是index页面中数据
        model.addAttribute("list",jsonObjects);
        return "index/index";
    }
}

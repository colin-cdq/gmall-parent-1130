package com.cdq.product.service.impl;

import com.cdq.model.product.BaseCategoryView;
import com.cdq.product.mapper.BaseCategory1Mapper;
import com.cdq.product.mapper.BaseCategory2Mapper;
import com.cdq.product.mapper.BaseCategory3Mapper;
import com.cdq.product.mapper.BaseCategoryViewMapper;
import com.cdq.product.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @program: gmall-parent-1130
 * @description:
 * @author: cdq
 * @create: 2021-05-30 21:36
 **/
@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    BaseCategory1Mapper baseCategory1Mapper;


    @Autowired
    BaseCategory2Mapper baseCategory2Mapper;


    @Autowired
    BaseCategory3Mapper baseCategory3Mapper;

    @Autowired
    BaseCategoryViewMapper baseCategoryViewMapper;

    @Override
    public List<BaseCategoryView> getCategoryView() {
        List<BaseCategoryView> baseCategoryViews = baseCategoryViewMapper.selectList(null);
        return baseCategoryViews;
    }

}

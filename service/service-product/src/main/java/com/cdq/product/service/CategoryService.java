package com.cdq.product.service;

import com.cdq.model.product.BaseCategoryView;

import java.util.List;

/**
 * @program: gmall-parent-1130
 * @description:
 * @author: cdq
 * @create: 2021-05-30 21:35
 **/
public interface CategoryService {
    List<BaseCategoryView> getCategoryView();
}

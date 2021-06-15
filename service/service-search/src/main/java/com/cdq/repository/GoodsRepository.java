package com.cdq.repository;

import com.cdq.model.list.Goods;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * @program: gmall-parent-1130
 * @description:
 * @author: cdq
 * @create: 2021-05-31 01:09
 **/
@Mapper
public interface GoodsRepository  extends ElasticsearchRepository<Goods,Long> {
}

package com.cdq.uesr.service.impl;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cdq.feign.ProductFeignClient11111;
import com.cdq.uesr.mapper.CartInfoMapper;
import com.cdq.model.cart.CartInfo;
import com.cdq.model.product.SkuInfo;
import com.cdq.uesr.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * @program: gmall-parent-1130
 * @description:
 * @author: cdq
 * @create: 2021-06-03 01:16
 **/
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    CartInfoMapper cartInfoMapper;

    @Autowired
    ProductFeignClient11111 productFeignClient;

    @Autowired
    RedisTemplate redisTemplate;

    @Override
    public CartInfo addCart(Long skuId , Long skuNum , String userId) {

        //查询sku信息
        SkuInfo skuById = productFeignClient.getSkuById(skuId);


        CartInfo cartInfo = new CartInfo ();
        //创建表的实体类对象
        cartInfo.setSkuNum(skuNum.intValue());
        cartInfo.setSkuId(skuId);
        cartInfo.setUserId(userId);
        cartInfo.setSkuName(skuById.getSkuName());
        cartInfo.setImgUrl(skuById.getSkuDefaultImg());
        cartInfo.setCartPrice(skuById.getPrice().multiply(new BigDecimal(skuNum)));
//        //创建条件构造器
//
//        //数据库只能有一个结果
//       CartInfo cartInfoDB = cartInfoMapper.selectOne ( queryWrapper );

        // 以缓存内容为主(购物车功能是缓存库，数据库用来备份)
        CartInfo cartInfoCache = (CartInfo) redisTemplate.opsForHash().get("user:" + userId + ":cart", skuId + "");

        //根据缓存判断
        if (cartInfoCache==null){
            //同步到数据库，先写数据库使用mysql主键自增
            cartInfoMapper.insert(cartInfo);
        }else {
            //更新操作
            QueryWrapper<CartInfo> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId);
            queryWrapper.eq("sku_id", skuId);
            //上面条件成立，修改SkuNum、CartPrice
            cartInfo.setSkuNum(cartInfoCache.getSkuNum() + skuNum.intValue());
            cartInfo.setCartPrice(skuById.getPrice().multiply(new BigDecimal(cartInfo.getSkuNum())));
            //使用修改数据添加到数据库
            cartInfoMapper.update(cartInfo, queryWrapper);
        }

        //redis写入（rendis如果写在数据库前面，就需要写一个自增的主键）
        // 写入缓存,更新和插入是同一个方法
        cartInfo.setSkuPrice(skuById.getPrice());
        // 以缓存内容为主(购物车功能是缓存库，数据库用来备份)
        redisTemplate.opsForHash().put("user:" + userId + ":cart", skuId + "", cartInfo);

        return cartInfo;
    }

    @Override
    public List<CartInfo> cartList(String userId) {

        // 先查询缓存
        List<CartInfo> cartInfos = (List<CartInfo>) redisTemplate.opsForHash().values("user:" + userId + ":cart");

        if (null == cartInfos || cartInfos.size() <= 0) {
            QueryWrapper<CartInfo> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId);
            List<CartInfo> cartInfosDB = cartInfoMapper.selectList(queryWrapper);

            if (null != cartInfosDB && cartInfosDB.size() > 0) {
                Map<String, Object> map = new HashMap<>();
                for (CartInfo cartInfo : cartInfosDB) {
                    cartInfo.setSkuPrice(productFeignClient.getSkuPriceById(cartInfo.getSkuId()));
                    map.put(cartInfo.getSkuId() + "", cartInfo);
                }
                redisTemplate.opsForHash().putAll("user:" + userId + ":cart", map);
            }
        }
        return cartInfos;
    }

    @Override
    public void checkCart(String userId , Long skuId , String isChecked) {
        //数据从缓存中查询
        CartInfo cartInfo = (CartInfo)redisTemplate.opsForHash().get("user:" + userId + ":cart", skuId + "");

        //获取id
        Long cartInfoId = cartInfo.getId ();


        //id设置被选中状态 ，isChecked是被选中会发生变化，从前端传递过来，在他添加到数据库和缓存中
        //id=商品  商品状态发生改变，修改数据库和redis
        cartInfo.setIsChecked(Integer.parseInt(isChecked));

        // 修改缓存的选中状态
        redisTemplate.opsForHash().put("user:" + userId + ":cart", skuId + "",cartInfo);


        QueryWrapper queryWrapper = new QueryWrapper<CartInfo>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.eq("sku_id", skuId);
        cartInfoMapper.update(cartInfo, queryWrapper);

        // 同步数据库
   //     cartInfoMapper.updateById (cartInfo );

    }

    @Override
    public List<CartInfo> getTradeOrder(String userId) {
        List<CartInfo> cartInfos = (List<CartInfo>)redisTemplate.opsForHash().values("user:"+userId+":cart");

        Iterator<CartInfo> iterator = cartInfos.iterator();

        // 对集合中的元素进行删除的时候需要用迭代器
        //.hasNext()只是判断是否存在下一个元素
        while(iterator.hasNext()){
            //不使用索引删除，因为3可能变成2,.next()指针下移，返回该指针所指向的元素
            CartInfo next = iterator.next();
            if(next.getIsChecked()==0){
                iterator.remove();// 未被选中的购物车选项删除
            }
        }
        return cartInfos;
    }


}

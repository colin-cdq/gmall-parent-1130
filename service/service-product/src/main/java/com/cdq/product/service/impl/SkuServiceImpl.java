package com.cdq.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cdq.aop.GmallCache;
import com.cdq.model.list.Goods;
import com.cdq.model.product.SkuAttrValue;
import com.cdq.model.product.SkuImage;
import com.cdq.model.product.SkuInfo;
import com.cdq.model.product.SkuSaleAttrValue;
import com.cdq.product.mapper.SkuAttrValueMapper;
import com.cdq.product.mapper.SkuImageMapper;
import com.cdq.product.mapper.SkuInfoMapper;
import com.cdq.product.mapper.SkuSaleAttrValueMapper;
import com.cdq.product.service.SkuService;
import com.cdq.search.client.SearchFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @program: gmall-parent-1130
 * @description:
 * @author: cdq
 * @create: 2021-05-20 15:20
 **/
@Service
public class SkuServiceImpl implements SkuService {
    @Autowired
    SkuInfoMapper skuInfoMapper;

    @Autowired
    SkuImageMapper skuImageMapper;

    @Autowired
    SkuAttrValueMapper skuAttrValueMapper;

    @Autowired
    SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    SearchFeignClient searchFeignClient;

    @Override
    public void saveSkuInfo(SkuInfo skuInfo) {

        // 保存sku
        skuInfoMapper.insert(skuInfo);

        //生成sku主键
        Long skuId = skuInfo.getId();

        // 根据主键保存skuImage
        for (SkuImage skuImage : skuInfo.getSkuImageList()) {
            skuImage.setSkuId(skuId);
            skuImageMapper.insert(skuImage);
        }

        // 根据主键保存skuAttrValue
        for (SkuAttrValue skuAttrValue : skuInfo.getSkuAttrValueList()) {
            skuAttrValue.setSkuId(skuId);
            skuAttrValueMapper.insert(skuAttrValue);
        }

        // 根据主键保存销售属性
        for (SkuSaleAttrValue skuSaleAttrValue : skuInfo.getSkuSaleAttrValueList()) {
            skuSaleAttrValue.setSkuId(skuId);
            skuSaleAttrValue.setSpuId(skuInfo.getSpuId());
            skuSaleAttrValueMapper.insert(skuSaleAttrValue);
        }

    }

    @Override
    public IPage<SkuInfo> list(Long page , long limit) {
        //创建分页器
        IPage<SkuInfo> infoIPage = new Page<>(page,limit);

        IPage<SkuInfo> infoIPageResult = skuInfoMapper.selectPage ( infoIPage , null );
        return infoIPageResult;
    }

    @Override
    public void onSale(Long skuId) {
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(skuId);
        skuInfo.setIsSale(1);
        skuInfoMapper.updateById(skuInfo);

        // 同步搜索引擎(搜索服务器，添加该商品)
        searchFeignClient.onSale(skuId);
    }

    @Override
    public void cancelSale(Long skuId) {
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(skuId);
        skuInfo.setIsSale(0);
        skuInfoMapper.updateById(skuInfo);

        // 同步搜索引擎(搜索服务器，删除该商品)
        searchFeignClient.cancelSale(skuId);
    }

    @GmallCache
    @Override
    public BigDecimal getSkuPriceById(Long skuId) {

        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);

        return skuInfo.getPrice();

    }

    @GmallCache
    @Override
    public List<SkuImage> getSkuImagesBySkuId(Long skuId) {

        QueryWrapper<SkuImage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sku_id",skuId);
        List<SkuImage> skuImages = skuImageMapper.selectList(queryWrapper);
        return skuImages;
    }

    @GmallCache
    @Override
    public SkuInfo getSkuById(Long skuId) {
        SkuInfo skuInfo = null;
        // 查询数据库
        skuInfo = skuInfoMapper.selectById(skuId);
        return skuInfo;
    }



    public SkuInfo getSkuByIdBak(Long skuId) {

        SkuInfo skuInfo = null;
        // 查询缓存
        skuInfo = (SkuInfo)redisTemplate.opsForValue().get("sku:"+skuId+":info");
        if(null==skuInfo){
            //创建锁
            String lockTag = UUID.randomUUID().toString();

            //分布式锁  第一个参数为键   第二个参数为值
            Boolean ifLock = redisTemplate.opsForValue().setIfAbsent("Sku:" + skuId + ":lock", lockTag, 1, TimeUnit.SECONDS);// 1秒后如果没有执行完毕，则自动删除锁
            //判断分布式锁iflook
            if (ifLock){
                //为true，可以获得缓存
                System.out.println(Thread.currentThread().getName() + "同学拿到分布式锁，congratulation!");
                // 查询数据库
                skuInfo = skuInfoMapper.selectById(skuId);

                if (null != skuInfo){ //避免缓存穿透，数据库中有数据
                    // 同步到缓存
                    redisTemplate.opsForValue().set("sku:"+skuId+":info",skuInfo);

                }else {
                    //数据库中没有数据,添加假缓存，设置10秒钟后过期，这个请求在次重新进入，一直循环，直到有这个key
                    redisTemplate.opsForValue().set ( "sku:"+skuId+":info" ,new SkuInfo(), 10, TimeUnit.SECONDS);
                }

                // 归还同步锁，判断当前的锁值是否是创建时的锁值
//                String currentLockTag = (String)redisTemplate.opsForValue().get("Sku:" + skuId + ":lock");
//                if(!StringUtils.isEmpty(currentLockTag)&&currentLockTag.equals(lockTag)){
//                    //归还同步锁，
//                    redisTemplate.delete ( "Sku:" + skuId + ":lock" );
//                }

                // lua脚本
                String luaScript = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                // 设置lua脚本返回的数据类型
                DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
                redisScript.setResultType(Long.class);
                // 设置lua脚本返回类型为Long
                redisScript.setScriptText(luaScript);
                redisTemplate.execute(redisScript, Arrays.asList("Sku:" + skuId + ":lock"), lockTag);

            }else {
                // 自旋
                // 未拿到锁的请求，等待若干秒后，重新访问自己
                System.out.println(Thread.currentThread().getName() + "同学未拿到分布式锁，开始自旋。。。");
                //等待3秒
                try {
                    Thread.sleep ( 3000 );
                } catch (InterruptedException e) {
                    e.printStackTrace ();
                }
                //无线循环直到拿到锁，
                // getSkuById(skuId );

                //如果拿到锁结束使用return
                return getSkuById(skuId );
            }

        }
        return skuInfo;
    }

    @GmallCache
    @Override
    public List<Map<String, Object>> getValuesSku(Long spuId) {


        List<Map<String, Object>> maps = skuSaleAttrValueMapper.selectValuesSku(spuId);

        return maps;
    }

    @Override
    public Goods getGoodsBySkuId(Long skuId) {
        Goods goods = skuInfoMapper.getGoodsBySkuId(skuId);

        return goods;
    }


}

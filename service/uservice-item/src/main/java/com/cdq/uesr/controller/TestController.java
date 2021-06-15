package com.cdq.uesr.controller;



import com.cdq.model.product.SkuInfo;
import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @program: gmall-parent-1130
 * @description:
 * @author: cdq
 * @create: 2021-05-27 14:15
 **/
public class TestController {
    public static void main(String[] args) {
//        new Thread (()->{
//
//        }).start ();

        CompletableFuture.supplyAsync ( new Supplier<SkuInfo> () {
            @Override
            public SkuInfo get() {
//                try {
//                    Thread.sleep ( 100 );
//                } catch (InterruptedException e) {
//                    e.printStackTrace ();
//                }

                System.out.println (Thread.currentThread ().getName () + "skuInfo线程查询skuInfo" );

                //创建skuInfo
                SkuInfo skuInfo = new SkuInfo ();
                //名字赋值
                skuInfo.setSkuName ( "联想r7000p" );
                //价格赋值
                skuInfo.setPrice ( new BigDecimal ( "5999" ) );

                int i =1/0;
                return skuInfo;
            }
        } ).thenApplyAsync ( new Function<SkuInfo, SkuInfo> () {
            @Override
            public SkuInfo apply(SkuInfo skuInfo) {
                System.out.println ( Thread.currentThread ().getName () + "then线程检查" + skuInfo.getSkuName () );

                return skuInfo;
            }
        } ).exceptionally ( new Function<Throwable, SkuInfo> () {
            @Override
            public SkuInfo apply(Throwable throwable) {
                System.out.println(Thread.currentThread().getName()+"异常线程:");
                SkuInfo skuInfo = new SkuInfo();
                skuInfo.setPrice(new BigDecimal(0));

                return skuInfo;
            }
        } ).whenComplete ( new BiConsumer<SkuInfo, Throwable> () {
            //return skuInfo之后才能.whenComplete
            @Override
            public void accept(SkuInfo skuInfo , Throwable throwable) {

                System.out.println ( Thread.currentThread ().getName () +"when线程原价：" + skuInfo.getPrice () );
                System.out.println ( Thread.currentThread ().getName () +"when线程活动优惠：" + skuInfo.getPrice ().multiply ( new BigDecimal ( "0.8" ) ) );

            }

        } );


        //线程组合
  //   CompletableFuture.allOf ( ).join ();


        try {
            Thread.sleep ( 2000 );
        } catch (InterruptedException e) {
            e.printStackTrace ();
        }

        //主线程
        System.out.println (Thread.currentThread ().getName () + "主线程检索商品详情结束");
    }
}

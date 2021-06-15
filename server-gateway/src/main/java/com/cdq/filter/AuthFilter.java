package com.cdq.filter;

import com.alibaba.fastjson.JSONObject;
import com.cdq.common.util.Result;
import com.cdq.common.util.ResultCodeEnum;
import com.cdq.user.client.UserFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @program: gmall-parent-1130
 * @description:
 * @author: cdq
 * @create: 2021-06-04 00:13
 **/
@Component
public class AuthFilter implements GlobalFilter {
    @Autowired
    UserFeignClient userFeignClient;

    AntPathMatcher antPathMatcher = new AntPathMatcher();

    //获取配置文件中的白名单
    @Value("${authUrls.url}")
    String authUrls;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        //创建请求
        ServerHttpRequest request = exchange.getRequest();
        //创建响应
        ServerHttpResponse response = exchange.getResponse();

        //获取请求链接
        String uri = request.getURI().toString ();
        String path = request.getPath().toString();

        //发行所有静态资源
        if(uri.contains(".jpg")||uri.contains(".png")||uri.contains(".ico")||uri.contains(".js")||uri.contains(".css")||uri.contains("passport")){
            return chain.filter(exchange);
        }

        // 内部请求
        //路径中带有inner直接返回mono给请求端
        if(antPathMatcher.match("/**/inner/**",path)){
            return out(response, ResultCodeEnum.SECKILL_ILLEGAL);//直接返回mono给请求端
        }

        // 不管该功能是否需要登录，都进行登录认证,并且将用户userId传递到后台
        String token = getCookieOrHeaderValue(request, "token");
        Map<String, Object> verifyMap = new HashMap<> ();
        //判断不为null
        if (!StringUtils.isEmpty(token)) {
            //从数据库或缓存中获取token
            verifyMap = userFeignClient.verify(token);
            //获取数据
            String success = (String) verifyMap.get("success");
            String userId = (String) verifyMap.get("userId");
            //判断获取的值是success
            if(!StringUtils.isEmpty(success) && success.equals("success")){
                // 验证成功，将用户userId传递到后台
                request.mutate().header("userId",userId).build();
                exchange.mutate().request(request).build();
            }
        }

        // 如果用户从未登陆过或者没有验证成功,将userTempId传递到请求chain.filter();
        String userTempId = getCookieOrHeaderValue(request, "userTempId");
        if(!StringUtils.isEmpty(userTempId)){
            // 有userTempId，将用户userTempId传递到后台
            request.mutate().header("userTempId",userTempId).build();
            exchange.mutate().request(request).build();
        }


        //wep请求
        //split:分割  按照，分割
        String[] split =  authUrls.split ( "," );
        //遍历
        for (String authUrl : split) {
            //请求链接
            if(uri.contains (authUrl)){
                // 如果当前请求包含在白名单中，则需要进行身份验证
//                Map<String,Object> verifyMap = userFeignClient.verify ( getCookieOrHeaderValue(request,"token"));

                //从网关获取用户信息
                String success = (String)verifyMap.get("success");
                String userId = (String)verifyMap.get("userId");
                //判断
                // 如果当前请求包含在白名单中，则需要进行身份验证
                if (StringUtils.isEmpty(success) || !success.equals("success")) {
                    // 重定向到登陆页面
                    response.setStatusCode( HttpStatus.SEE_OTHER);
                    //原始请求地址
//                    //链接中的请求参数变成string
                    URI uriTemp = request.getURI();
                    String uriTempStr = uriTemp.toString();
                    response.getHeaders().set(HttpHeaders.LOCATION, "http://passport.gmall.com/login.html?originUrl=" + uriTempStr);
                    Mono<Void> voidMono = response.setComplete();
                    return voidMono;
                }

            }
        }

        return chain.filter(exchange);
    }

    //从cokie中拿到数据的方法
    private String getCookieOrHeaderValue(ServerHttpRequest request ,String token) {

        String tokenResult = "";
        MultiValueMap<String, HttpCookie> cookies = request.getCookies();

        if (null != cookies && cookies.size() > 0) {
            List<HttpCookie> tokens = cookies.get(token);
            if (null != tokens && tokens.size() > 0) {
                for (HttpCookie httpCookie : tokens) {
                    tokenResult = httpCookie.getValue();
                }
            }
        }

        // 如果是异步请求，那么cookie中没有token，只能通过header获取，userTempId默认在header中(此处多做了一步操作)
        if(StringUtils.isEmpty(tokenResult)){
            List<String> strings = request.getHeaders().get(token);
            if(null!=strings&&strings.size()>0){
                tokenResult = strings.get(0);
            }
        }

        return tokenResult;
    }




    //mono给请求端
    private Mono<Void> out(ServerHttpResponse response, ResultCodeEnum resultCodeEnum) {
        Result<Object> result = Result.build(null, resultCodeEnum);
        byte[] bits = JSONObject.toJSONString(result).getBytes( StandardCharsets.UTF_8);
        DataBuffer wrap = response.bufferFactory().wrap(bits);
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");// 设置编码格式
        // 输入到页面
        Mono<Void> voidMono = response.writeWith(Mono.just(wrap));
        return voidMono;
    }
}

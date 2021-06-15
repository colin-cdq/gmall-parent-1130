package com.cdq.payment.controleller;

import com.cdq.model.payment.PaymentInfo;
import com.cdq.payment.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @program: gmall-parent-1130
 * @description:
 * @author: cdq
 * @create: 2021-06-07 03:00
 **/
@RestController
@RequestMapping("api/payment/")
public class PaymentApiController {

    @Autowired
    PaymentService paymentService;

    @RequestMapping("alipay/submit/{orderId}")
    public String alipay(HttpServletRequest request, @PathVariable("orderId") String orderId) {
        String userId = request.getHeader("userId");
        String form =  paymentService.alipayForm(userId,orderId);

//        //检查信息form中有，所以把查询方法写在实现类方法里就可以了
//        PaymentInfo paymentInfo = new PaymentInfo ();
//
//        paymentService.savaPaymen();
//
//        paymentService.sendPaymentDelayMessage (  );

        return form;// 提交支付时，要通知消息队列，检查支付结果
    }

    /*
    * 支付后跳转页面
    * */

    @RequestMapping("alipay/callback/return")
    public String callback(HttpServletRequest request) {

        //使用路劲传参数
        String out_trade_no = request.getParameter("out_trade_no");
        String trade_no = request.getParameter("trade_no");

        //获取所有的参数
        String callback_content = request.getQueryString();

        //创建付款信息对象，赋值传入修改的方法
        PaymentInfo paymentInfo = new PaymentInfo ();
        //把request获取的参数传递过来
        paymentInfo.setOutTradeNo(out_trade_no);
        paymentInfo.setTradeNo(trade_no);
        paymentInfo.setCallbackContent(callback_content);

        //修改支付状态
        paymentService.updatePayment(paymentInfo);

        Map<String,Object> map = new HashMap<> ();
        //把上面的参数信息放入map集合中
        map.put("paymentId",paymentInfo.getId());
        map.put("trade_no",paymentInfo.getTradeNo());
        map.put("out_trade_no",paymentInfo.getOutTradeNo());
        paymentService.sendPaymentMessage("exchange.direct.payment.pay","payment.pay",map);// 成功支付后，要通知消息队列，XX订单已经支付状态

        return "成功页面";
    }

}

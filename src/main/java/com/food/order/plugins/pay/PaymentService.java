package com.food.order.plugins.pay;

import com.food.order.model.entity.OrderGoods;
import com.food.order.model.entity.Orders;
import com.food.order.model.entity.PaymentsConfig;
import com.food.order.model.entity.Plugins;
import com.food.order.plugins.PluginsData;
import org.springframework.core.annotation.Order;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface PaymentService {

    public void setParams(PaymentsConfig paymentsConfig,String host);

    public PluginsData getPaySendData(HttpServletRequest request,String order_no,Integer total_fee);//获取支付要发送的数据

    public PluginsData doPay(PluginsData data, HashMap<String, Object> map);//支付

    public PluginsData callback(HttpServletRequest request,Orders orders);//支付结果回调

    public PluginsData getRefundSendData(String order_no,String out_order_no,String refund_order_no,Integer total_fee,Integer refund_fee);//获取退款要发送的数据

    public PluginsData refund(PluginsData payData);//退款



    public List<Integer> canPayAgentTypes();

    public int getPayOrCollect();


}

package com.food.order.plugins.pay.balance;

import com.alibaba.fastjson.JSON;
import com.food.order.model.entity.Consumer;
import com.food.order.model.entity.Orders;
import com.food.order.model.entity.PaymentsConfig;
import com.food.order.model.entity.User;
import com.food.order.plugins.*;
import com.food.order.plugins.pay.PayOrCollectEnum;
import com.food.order.plugins.pay.PayTarget;
import com.food.order.plugins.pay.PaymentService;
import com.food.order.utils.utils.MD5Util;
import com.food.order.utils.utils.https.HttpClientUtil;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 余额支付
 */
@PayTarget
public class Balance extends BasePlugins implements PaymentService, PluginsService {
    protected String uid;//用户ID
    protected String key;
    protected String host;
    public Balance(){//必须要空参的构造，否则会造成反射实例异常

    }

    public void setParams(PaymentsConfig paymentsConfig,String host) {
        if(paymentsConfig != null) {
            uid = (String) paymentsConfig.getParamsMap().get("uid");
            key = getParam("key", paymentsConfig.getParams());
            this.host = host;
            //oauthUser = paymentsConfig.getOauthUser();
        }

    }
    @Override
    public PluginsData getPaySendData(HttpServletRequest request, String order_no, Integer total_fee){
        PluginsData payData = new PluginsData();
        Integer balance = (Integer)request.getAttribute("balance");
        if(balance < total_fee){
            payData.setCode(100);
            payData.setMsg("余额不足,请先充值");
            return payData;
        }
        payData.getData().put("order_no",order_no);
        payData.getData().put("total_fee",total_fee);
        return payData;
    }
    @Override
    public PluginsData doPay(PluginsData payData,HashMap<String, Object> map){
        PluginsData payData1 = new PluginsData();
        String order_no = (String) payData.getData().get("order_no");
        Integer total_fee = (Integer) payData.getData().get("total_fee");
        Long ts = System.currentTimeMillis();
        Map<String, String> responBodyMap = new HashMap<>();
        responBodyMap.put("uid", uid);
        responBodyMap.put("ts", ""+ts);
        responBodyMap.put("total_fee",""+total_fee);
        responBodyMap.put("order_no", order_no);
        String sign = getSign(responBodyMap, key);

        String callbackUrl = host+"/api/payment/callback/"+order_no+"?sign="+sign+"&ts="+ts+"&total_fee="+total_fee;

        new Thread(){ //异步调用回调支付结果
            @Override
            public void run() {
                new HttpClientUtil().doGet(callbackUrl,"UTF-8");
                super.run();
            }
        }.start();
        map.put("price",Double.valueOf(total_fee)/100);
        map.put("msg","正在支付中,稍后请刷新订单查看支付结果");
        payData1.getData().put("url","pay/balance/balance");
        return payData1;
    }
    /**
     * 回调处理
     * @param request
     * @return
     */
    public PluginsData callback(HttpServletRequest request, Orders orders){
        PluginsData  payData = new PluginsData();
        String ts = request.getParameter("ts");
        String orderNo = orders.getOrderNo();
        Integer total_fee = Integer.parseInt(request.getParameter("total_fee"));
        String sign = request.getParameter("sign");
        Map<String, String> responBodyMap = new HashMap<>();
        responBodyMap.put("uid", ""+orders.getPayUser().getId());
        responBodyMap.put("ts", ""+ts);
        responBodyMap.put("total_fee",""+total_fee);
        responBodyMap.put("order_no", orderNo);
        String nowSign = getSign(responBodyMap, key);

        if(sign == null && sign.equals(nowSign)){
            payData.setCode(100);
            payData.setMsg("签名错误");
            payData.getData().put("result","fail");
            return payData;
        }
        Integer realPrice = orders.getRealPrice();
        if(!realPrice.equals(total_fee)){
            payData.setCode(100);
            payData.setMsg("金额不对");
            payData.getData().put("result","fail");
            return payData;
        }
        orders.setPayTime(new Date());
        payData.getData().put("result","success");
        return payData;
    }
    public PluginsData getRefundSendData(String order_no,String out_order_no,String refund_order_no,Integer total_fee,Integer refund_fee){
        PluginsData payData = new PluginsData();
        return payData;
    }
    @Autowired
    public PluginsData refund(PluginsData  payData){
        PluginsData  payData1 = new PluginsData();
        return payData1;
    }


    @Override
    public Map<String,String> getParamsConfig() {
        Map<String, String>  map = new HashMap<>();
        map.put("key","自定义支付密钥key");
        return map;
    }


    @Override
    public List<Integer> canPayAgentTypes() {
        List<Integer> types = new ArrayList<>();
        types.add(AgnetTypeEnum.WX_APP.getIndex());
        types.add(AgnetTypeEnum.PC_APP.getIndex());
        types.add(AgnetTypeEnum.WX_WEB.getIndex());
        types.add(AgnetTypeEnum.MOBILE_WEB.getIndex());
        types.add(AgnetTypeEnum.PC_WEB.getIndex());
        return types;
    }

    @Override
    public int getPayOrCollect() {
        return PayOrCollectEnum.Pay.getIndex();
    }

    /**
     * 生成sign
     * @param options
     * @param partnerKey
     * @return
     */
    protected String getSign(Map<String,String> options,String partnerKey){
        String result = "";
        try {
            List<Map.Entry<String, String>> infoIds = new ArrayList<Map.Entry<String, String>>(options.entrySet());
            // 对所有传入参数按照字段名的 ASCII 码从小到大排序（字典序）
            Collections.sort(infoIds, new Comparator<Map.Entry<String, String>>() {

                public int compare(Map.Entry<String, String> o1, Map.Entry<String, String> o2) {
                    return (o1.getKey()).toString().compareTo(o2.getKey());
                }
            });

            // 构造签名键值对的格式
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, String> item : infoIds) {
                if (item.getKey() != null || item.getKey() != "") {
                    String key = item.getKey();
                    Object val = item.getValue();
                    if (!(val == "" || val == null)) {
                        sb.append(key + "=" + val+"&");
                    }
                }

            }
            result = sb.toString();
            //进行MD5加密
            result = MD5Util.MD5Encode(result+"key="+partnerKey).toUpperCase();
        } catch (Exception e) {
            return null;
        }
        return result;
    }

    @Override
    public String getPluginsTag() {
        return Balance.class.getName();
    }

    @Override
    public int getPluginsType() {
        return PluginsTypeEnum.PAY_PLUGINS.getIndex();
    }

    @Override
    public Integer getVip0Price() {
        return 0;
    }

    @Override
    public Integer getVip1Price() {
        return 0;
    }

    @Override
    public Integer getVip2Price() {
        return 0;
    }

    @Override
    public int getExpiryDay() {
        return 0;
    }

    @Override
    public int getFreeExpiryDay() {
        return 0;
    }

    @Override
    public String getName() {
        return "余额支付";
    }

    @Override
    public String getPicPath() {
        return null;
    }

    @Override
    public String getNote() {
        return "客人余额帐户支付";
    }

    @Override
    public String getPluginsUUID() {
        try {
            return MD5Util.MD5Encode(Balance.class.getName());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
}

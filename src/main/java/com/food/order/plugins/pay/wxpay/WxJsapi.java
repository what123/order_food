package com.food.order.plugins.pay.wxpay;

import com.food.order.model.entity.*;
import com.food.order.plugins.*;
import com.food.order.plugins.pay.*;
import com.food.order.utils.utils.MD5Util;
import com.food.order.utils.utils.https.HttpClientUtil;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * jsapi支付
 */
@PayTarget
public class WxJsapi extends BasePlugins implements PaymentService, PluginsService {
    protected String mchid;
    private String appid;
    protected String key;
    protected String p12Path;
    protected String host;

    //protected OauthUser oauthUser = null;
    public WxJsapi(){//必须要空参的构造，否则会造成反射实例异常

    }

    public void setParams(PaymentsConfig paymentsConfig,String host) {
        if(paymentsConfig != null) {
            mchid = getParam("mchid", paymentsConfig.getParams());
            appid = getParam("appid", paymentsConfig.getParams());
            p12Path = getParam("p12", paymentsConfig.getParams());
            key = getParam("key", paymentsConfig.getParams());
            this.host = host;
            //oauthUser = paymentsConfig.getOauthUser();
        }

    }
    @Override
    public PluginsData getPaySendData(HttpServletRequest request, String order_no, Integer total_fee){
        PluginsData payData = new PluginsData();
        Map<String, String> responBodyMap = new HashMap<>();
        responBodyMap.put("appid", appid);
        responBodyMap.put("mch_id", mchid);
        String nonce_str = createNonceStr(32);
        responBodyMap.put("nonce_str", nonce_str);
        responBodyMap.put("body", "餐费买单");
        responBodyMap.put("spbill_create_ip", new PayUtil().getIpAddr(request));
        responBodyMap.put("total_fee", "" + total_fee);
        responBodyMap.put("out_trade_no", order_no);
        responBodyMap.put("trade_type", "JSAPI");
        responBodyMap.put("notify_url", host+"/api/payment/callback/"+order_no);
        String sign = getSign(responBodyMap, key);
        responBodyMap.put("sign", sign);


        payData.getData().put("responBodyMap",responBodyMap);
        payData.getData().put("order_no",order_no);
        payData.getData().put("total_fee",total_fee);
        return payData;
    }
    @Override
    public PluginsData doPay(PluginsData payData,HashMap<String, Object> map){
        PluginsData payData1 = new PluginsData();
//        if(oauthUser == null){
//            payData1.setCode(100);
//            payData1.setMsg("oauth授权未成功");
//            return payData1;
//        }


        Map<String, String> responBodyMap = (Map<String, String>) payData.getData().get("responBodyMap");
        String xml = map2xml(responBodyMap);
        Integer total_fee = (Integer) payData.getData().get("total_fee");

        String data = new HttpClientUtil().doPostRawXml("https://api.mch.weixin.qq.com/pay/unifiedorder", xml);

        //String data = new HttpClientUtil().doPostRawXml("https://api.mch.weixin.qq.com/pay/unifiedorder", xml);
        Map<String,String>  rsMap = xml2map(data);
        try {
            if(rsMap.get("return_code").equals("SUCCESS") && rsMap.get("result_code").equals("SUCCESS")){
                map.put("appId",rsMap.get("appid"));
                map.put("timeStamp",System.currentTimeMillis()/1000);
                map.put("nonceStr",rsMap.get("nonce_str"));
                map.put("signType","MD5");
                map.put("paySign",rsMap.get("sign"));
                map.put("package","prepay_id="+rsMap.get("prepay_id"));
                map.put("price",Double.valueOf(total_fee)/100);
                payData1.getData().put("url","pay/wx/jsapi");
            }else{
                payData1.setCode(100);
                payData1.setMsg("微信支付jsapi接口:"+rsMap.get("return_msg"));
            }
        } catch (Exception e) {
            payData1.setCode(100);
            payData1.setMsg("生成支付码异常");

        }
        return payData1;
    }

    private String getSandBoxKey(String mch_id,String nonce_str){
        Map<String,String> map = new HashMap<>();
        map.put("mch_id",mch_id);
        map.put("nonce_str",nonce_str);
        map.put("sign_type","MD5");
        String sign = getSign(map,"8LYk44MYuKT77R6e2iQDsvghK8NpnbGM");
        map.put("sign",sign);

        String xml = map2xml(map);
        String url = "https://api.mch.weixin.qq.com/sandboxnew/pay/getsignkey";
        String data = new HttpClientUtil().doPostRawXml(url, xml);
        Map<String,String> map2 = xml2map(data);
        if(map2.get("return_code").equals("SUCCESS")){
            return map2.get("'sandbox_signkey");
        }else{
            return map2.get("''return_msg");
        }
    }
    /**
     * 回调处理
     * @param request
     * @return
     */
    public PluginsData callback(HttpServletRequest request, Orders orders){
        PluginsData  payData = new PluginsData();
        String data = readAsChars(request);

        Map<String,String> dataMap = xml2map(data);
        if(dataMap.get("return_code").equals("SUCCESS") && dataMap.get("result_code").equals("SUCCESS")){
            String sign = dataMap.get("sign");
            dataMap.remove("sign");
            String sign2 = getSign(dataMap,key);
            if(!sign.equals(sign2)){
                payData.setCode(100);
                payData.setMsg("签名错误");
                payData.getData().put("result","fail");
                return payData;
            }
            Integer realPrice = orders.getRealPrice();
            Integer total_fee = Integer.parseInt(dataMap.get("total_fee"));
            if(!realPrice.equals(total_fee)){
                payData.setCode(100);
                payData.setMsg("金额不对");
                payData.getData().put("result","fail");
                return payData;
            }
            orders.setOutOrderNo(dataMap.get("transaction_id"));
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                orders.setPayTime(simpleDateFormat.parse(dataMap.get("time_end")));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            orders.setPayCallBackResult(data);//支付结果
            payData.getData().put("result","success");
            return payData;
        }else{
            payData.setCode(100);
            payData.setMsg("微信支付jsapi接口:"+request.getParameter("return_msg"));
            payData.getData().put("result","fail");
            return payData;
        }
    }
    public PluginsData getRefundSendData(String order_no,String out_order_no,String refund_order_no,Integer total_fee,Integer refund_fee){
        PluginsData payData = new PluginsData();
        Map<String, String> responBodyMap = new HashMap<>();
        responBodyMap.put("appid", appid);
        responBodyMap.put("mch_id", mchid);
        responBodyMap.put("nonce_str", createNonceStr(5));
        responBodyMap.put("transaction_id",order_no);
        responBodyMap.put("out_refund_no",refund_order_no);
        responBodyMap.put("total_fee",""+(total_fee));
        responBodyMap.put("refund_fee",""+(refund_fee));
        String sign = getSign(responBodyMap, key);
        responBodyMap.put("sign", sign);
        payData.getData().put("responBodyMap",responBodyMap);
        payData.getData().put("order_no",order_no);
        return payData;
    }
    @Autowired
    public PluginsData refund(PluginsData  payData){
        Map<String, String> responBodyMap = (Map<String, String>) payData.getData().get("responBodyMap");

        String xml = map2xml(responBodyMap);
        PluginsData  payData1 = new PluginsData();
        if(p12Path == null || p12Path.trim().equals("") || !new File(p12Path).exists()){
            payData1.setCode(100);
            payData1.setMsg("请在配置中上传p12证书文件");
            return payData1;
        }
        String data = new CertHttpUtil().postData("https://api.mch.weixin.qq.com/secapi/pay/refund", map2xml(responBodyMap),mchid,p12Path);//("https://api.mch.weixin.qq.com/secapi/pay/refund", map2xml(responBodyMap));
        if(data == null){
            payData1.setCode(100);
            payData1.setMsg("请在配置中上传有效的p12证书文件");
            return payData1;
        }
        Map<String,String>  rsMap = xml2map(data);
        try {
            //getSandBoxKey(mchid,createNonceStr(32));
            if(rsMap.get("return_code").equals("SUCCESS")){
                if(rsMap.get("result_code").equals("SUCCESS")) {
                    return payData1;
                }else{
                    payData1.setCode(100);
                    payData1.setMsg("微信支付接口接口:"+rsMap.get("err_code_des"));
                }
            }else{
                payData1.setCode(100);
                payData1.setMsg("微信支付接口接口:"+rsMap.get("return_msg"));
            }
        } catch (Exception e) {
            payData1.setCode(100);
            payData1.setMsg("退款发生异常");

        }
        return payData1;
    }


    @Override
    public Map<String,String> getParamsConfig() {
        Map<String, String>  map = new HashMap<>();
        map.put("appid","公众号的appid");
        map.put("mchid","支付平台分配的mchid");
        map.put("key","支付平台分配的key");
        map.put("UPLOADFILE_p12","上传支付平台分配的证书p12(退款功能必须填写)");//UPLOADFILE_表示上传文件的控件，取值时直接使用p12取值
        return map;
    }

    @Override
    public List<Integer> canPayAgentTypes() {
        List<Integer> types = new ArrayList<>();
        types.add(AgnetTypeEnum.WX_WEB.getIndex());//微信浏览器可使用
        //types.add(AgnetTypeEnum.MOBILE_WEB.getIndex());//测试用
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

    /** map集合参数转换为xml格式
     * @param map 要转换的map对象
     * @return XML格式的字符串
     */
    private String map2xml(Map<String, String> map)
    {
        Document document = DocumentHelper.createDocument();
        Element element = document.addElement("xml");
        for(String key : map.keySet()) {
            Element appid = element.addElement(key);
            appid.setText(map.get(key));
        }
        String result = document.asXML();
        return result.substring(39, result.length());
    }
    public Map<String,String> xml2map(String xml){
        Map<String,String> map = new HashMap<String,String>();
        Document doc = null;
        try {
            doc = DocumentHelper.parseText(xml); // 将字符串转为XML  
            Element rootElt = doc.getRootElement(); // 获取根节点  
            List<Element> list = rootElt.elements();//获取根节点下所有节点  
            for(Element element:list){//遍历节点  
                 map.put(element.getName(),element.getText());//节点的name为map的key，text为map的value  
            }
        }catch(DocumentException e){
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    /** 生成随机数
     * @param count 要生成的随机数位数
     * @return 随机数字符串
     */
    private String createNonceStr(int count){
        String[] nums = {"0","1","2","3","4","5","6","7","8","9","A","B","C","D","E","F"};
        int maxIndex = nums.length - 1;
        int numIndex;
        StringBuilder builder = new StringBuilder(count);
        for (int i = 0; i < count; i++){
            numIndex = (int)(Math.random() * maxIndex);
            builder.append(nums[numIndex]);
        }
        return builder.toString();
    }

    // 字符串读取
    // 方法一
    public  String readAsChars(HttpServletRequest request)
    {

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder("");
        try
        {
            br = request.getReader();
            String str;
            while ((str = br.readLine()) != null)
            {
                sb.append(str);
            }
            br.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (null != br)
            {
                try
                {
                    br.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }

    @Override
    public String getPluginsTag() {
        return WxJsapi.class.getName();
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
        return "微信支付";
    }

    @Override
    public String getPicPath() {
        return null;
    }

    @Override
    public String getNote() {
        return "微信公众号支付插件";
    }

    @Override
    public String getPluginsUUID() {
        try {
            return MD5Util.MD5Encode(WxJsapi.class.getName());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
}

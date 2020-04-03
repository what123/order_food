package com.food.order.plugins.printer.feie;

import com.food.order.model.entity.Printer;
import com.food.order.plugins.BasePlugins;
import com.food.order.plugins.PluginsData;
import com.food.order.plugins.PluginsService;
import com.food.order.plugins.PluginsTypeEnum;
import com.food.order.plugins.printer.*;
import com.food.order.utils.utils.MD5Util;
import com.food.order.utils.utils.Sha1Util;
import com.food.order.utils.utils.https.HttpClientUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * 飞鹅打印机
 */
@PrinterTarget
public class PrivateFeie extends BasePlugins implements PrinterService, PluginsService {
    protected String user = "";//
    private String UKEY = "";//

    protected Map<String,String> getBaseParams(String apiname,Printer printer){
        Map<String,String> map = new HashMap<>();
        String user = getParam("user",printer.getApiConfigParams());
        map.put("user",user);
        String stime = ""+(System.currentTimeMillis()/1000);
        map.put("stime",stime);
        String UKEY = getParam("UKEY",printer.getApiConfigParams());
        map.put("sig", Sha1Util.SHA1Encode(user+UKEY+stime));
        map.put("apiname",apiname);
        return map;
    }

    @Override
    public PluginsData addPrinter(Printer printer) {
        PluginsData printerData = new PluginsData();
        Map<String,String> params = getBaseParams("Open_printerAddlist",printer);
        //打印机编号(必填) # 打印机识别码(必填) # 备注名称(选填) # 流量卡号码(选填)
        String paramsData = printer.getApiConfigParams();
        String sn = getParam("sn",printer.getApiConfigParams());
        String code = getParam("code",printer.getApiConfigParams());
        params.put("printerContent",sn+" # "+code+" # "+printer.getStore().getName()+"(id:"+printer.getStore().getId()+")-"+printer.getName());
        String data = new HttpClientUtil().doPostFormData("https://api.feieyun.cn/Api/Open/", params, "utf-8");
        JSONObject jsonObject = new JSONObject(data);
        if(jsonObject.getInt("ret") == 0){
            JSONArray okJsonArray =  jsonObject.getJSONObject("data").getJSONArray("ok");
            if(okJsonArray.length() > 0){
                return printerData;
            }else{
                printerData.setCode(100);
                printerData.setMsg(jsonObject.getJSONObject("data").getJSONArray("no").getString(0));
            }
        }else{
            printerData.setCode(100);
            printerData.setMsg(jsonObject.getString("msg"));
        }
        return printerData;
    }

    @Override
    public PluginsData delPrinter(Printer printer) {
        PluginsData printerData = new PluginsData();
        Map<String,String> params = getBaseParams("Open_printerDelList",printer);
        //打印机编号(必填) # 打印机识别码(必填) # 备注名称(选填) # 流量卡号码(选填)
        String sn = getParam("sn",printer.getApiConfigParams());
        params.put("snlist",sn);
        String data = new HttpClientUtil().doPostFormData("https://api.feieyun.cn/Api/Open/", params, "utf-8");
        JSONObject jsonObject = new JSONObject(data);
        if(jsonObject.getInt("ret") == 0){
            JSONArray okJsonArray =  jsonObject.getJSONObject("data").getJSONArray("ok");
            if(okJsonArray.length() > 0){
                return printerData;
            }else{
                printerData.setCode(100);
                printerData.setMsg(jsonObject.getJSONObject("data").getJSONArray("no").getString(0));
            }
        }else{
            printerData.setCode(100);
            printerData.setMsg(jsonObject.getString("msg"));
        }
        return printerData;
    }

    @Override
    public String getPrintData(String data) {
        /**
         <BR> ：换行符
         <CUT> ：切刀指令(主动切纸,仅限切刀打印机使用才有效果)
         <LOGO> ：打印LOGO指令(前提是预先在机器内置LOGO图片)
         <PLUGIN> ：钱箱或者外置音响指令
         <CB></CB>：居中放大
         <B></B>：放大一倍
         <C></C>：居中
         <L></L>：字体变高一倍
         <W></W>：字体变宽一倍
         <QR></QR>：二维码（单个订单，最多只能打印一个二维码）
         <RIGHT></RIGHT>：右对齐
         <BOLD></BOLD>：字体加粗
         **/
        data = data.replaceAll("###br###","<BR>");
        data = data.replaceAll("###cut###","<CUT>");
        data = data.replaceAll("###logo###","<LOGO>");
        data = data.replaceAll("###plugin###","<PLUGIN>");
        data = data.replaceAll("###cb###","<CB>");
        data = data.replaceAll("###/cb###","</CB>");
        data = data.replaceAll("###b###","<B>");
        data = data.replaceAll("###/b###","</B>");
        data = data.replaceAll("###c###","<C>");
        data = data.replaceAll("###/c###","</C>");
        data = data.replaceAll("###l###","<L>");
        data = data.replaceAll("###/l###","</L>");
        data = data.replaceAll("###w###","<W>");
        data = data.replaceAll("###/w###","<W>");
        data = data.replaceAll("###qr###","<QR>");
        data = data.replaceAll("###right###","<RIGHT>");
        data = data.replaceAll("###/right###","</RIGHT>");
        data = data.replaceAll("###bold###","<BOLD>");
        data = data.replaceAll("###/bold###","</BOLD>");

        return data;
    }

    @Override
    public PluginsData printData(Printer printer) {
        PluginsData printerData = new PluginsData();
        Map<String,String> params = getBaseParams("Open_printMsg",printer);
        //打印机编号(必填) # 打印机识别码(必填) # 备注名称(选填) # 流量卡号码(选填)
        String sn = getParam("sn",printer.getApiConfigParams());
        params.put("sn",sn);
        params.put("content",printer.getPrintData());
        String data = new HttpClientUtil().doPostFormData("https://api.feieyun.cn/Api/Open/", params, "utf-8");
        printerData.getData().put("result",data);
        JSONObject jsonObject = new JSONObject(data);
        if(jsonObject.getInt("ret") == 0){
            return printerData;
        }else{
            printerData.setCode(100);
            printerData.setMsg(jsonObject.getString("msg"));
        }
        return printerData;
    }

    @Override
    public PluginsData updatePrinter(Printer printer) {
        PluginsData printerData = new PluginsData();
        Map<String,String> params = getBaseParams("Open_printerEdit",printer);
        //打印机编号(必填) # 打印机识别码(必填) # 备注名称(选填) # 流量卡号码(选填)
        String sn = getParam("sn",printer.getApiConfigParams());
        params.put("sn",sn);
        params.put("name",printer.getStore().getName()+"(id:"+printer.getStore().getId()+")-"+printer.getName());
        String data = new HttpClientUtil().doPostFormData("https://api.feieyun.cn/Api/Open/", params, "utf-8");
        JSONObject jsonObject = new JSONObject(data);
        if(jsonObject.getInt("ret") == 0){
            return printerData;
        }else{
            printerData.setCode(100);
            printerData.setMsg(jsonObject.getString("msg"));
        }
        return printerData;
    }

    @Override
    public PluginsData clearPrintDataSqs(Printer printer) {
        PluginsData printerData = new PluginsData();
        Map<String,String> params = getBaseParams("Open_delPrinterSqs",printer);
        //打印机编号(必填) # 打印机识别码(必填) # 备注名称(选填) # 流量卡号码(选填)
        String sn = getParam("sn",printer.getApiConfigParams());
        params.put("sn",sn);
        String data = new HttpClientUtil().doPostFormData("https://api.feieyun.cn/Api/Open/", params, "utf-8");
        JSONObject jsonObject = new JSONObject(data);
        if(jsonObject.getInt("ret") == 0){
            return printerData;
        }else{
            printerData.setCode(100);
            printerData.setMsg(jsonObject.getString("msg"));
        }
        return printerData;
    }

    @Override
    public PluginsData quertPrintData(Printer printer) {
        return null;
    }

    @Override
    public PluginsData quertPrinterStatus(Printer printer) {
        PluginsData printerData = new PluginsData();
        Map<String,String> params = getBaseParams("Open_queryPrinterStatus",printer);
        //打印机编号(必填) # 打印机识别码(必填) # 备注名称(选填) # 流量卡号码(选填)
        String sn = getParam("sn",printer.getApiConfigParams());
        params.put("sn",sn);
        String data = new HttpClientUtil().doPostFormData("https://api.feieyun.cn/Api/Open/", params, "utf-8");
        JSONObject jsonObject = new JSONObject(data);
        if(jsonObject.getInt("ret") == 0){
            printerData.setMsg(jsonObject.getString("data"));//取到状态内容
            return printerData;
        }else{
            printerData.setCode(100);
            printerData.setMsg(jsonObject.getString("msg"));
        }
        return printerData;
    }


    @Override
    public Map<String, String> getParamsConfig() {
        Map<String, String>  map = new HashMap<>();
        map.put("user","飞鹅云后台注册用户名");
        map.put("UKEY","飞鹅云后台分配的UKEY");
        map.put("sn","打印机编号SN");
        map.put("code","打印机识别码");
        return map;
    }



    @Override
    public String getPluginsUUID() {
        try {
            return MD5Util.MD5Encode(getPluginsTag());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public String getPluginsTag() {
        return PrivateFeie.class.getName();
    }

    @Override
    public int getPluginsType() {
        return PluginsTypeEnum.PRINTER_PLUGINS.getIndex();
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
        return "飞鹅云打印机(自有后台)";
    }

    @Override
    public String getPicPath() {
        return null;
    }

    @Override
    public String getNote() {
        return "此插件使用了公用的飞鹅后台，可快速配置飞鹅云打印";
    }
}

package com.food.order.plugins.printer;

import com.alibaba.fastjson.JSON;
import com.food.order.model.entity.Goods;
import com.food.order.model.entity.OrderGoods;
import com.food.order.model.entity.Orders;
import com.food.order.plugins.PluginsService;
import com.food.order.plugins.pay.PayTarget;
import com.food.order.plugins.pay.PaymentService;
import org.reflections.Reflections;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class PrinterUtil {
    public List<PrinterService> getPrinterClass(String tag){
        List<PrinterService> payClass = new ArrayList<>();
        //入参 要扫描的包名
        Reflections f = new Reflections("com.food.order.plugins.printer");
        //入参 目标注解类
        Set<Class<?>> clazzes = f.getTypesAnnotatedWith(PrinterTarget.class);
        Iterator<Class<?>> it = clazzes.iterator();
        while (it.hasNext()) {
            Class<?> clazz = it.next();
            try {

                PluginsService pluginsService = (PluginsService) clazz.newInstance();
                if(tag == null || tag.equals(pluginsService.getPluginsTag())){
                    PrinterService printerService = (PrinterService) pluginsService;
                    payClass.add(printerService);
                }

            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return payClass;
    }
    public List<PrinterService> getPrinterClassByUUID(String uuid){
        List<PrinterService> payClass = new ArrayList<>();
        //入参 要扫描的包名
        Reflections f = new Reflections("com.food.order.plugins.printer");
        //入参 目标注解类
        Set<Class<?>> clazzes = f.getTypesAnnotatedWith(PrinterTarget.class);
        Iterator<Class<?>> it = clazzes.iterator();
        while (it.hasNext()) {
            Class<?> clazz = it.next();
            try {

                PluginsService pluginsService = (PluginsService) clazz.newInstance();
                if(uuid == null || uuid.equals(pluginsService.getPluginsUUID())){
                    PrinterService printerService = (PrinterService) pluginsService;
                    payClass.add(printerService);
                }

            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return payClass;
    }

    public String prasePrintData(String data, Orders orders,List<OrderGoods> orderGoodsList,boolean isAdd){
        if(data == null || data.trim().equals("")){
            return "";
        }
        int start = data.indexOf("###start_goods_content###");
        int end = data.indexOf("###end_goods_content###");
        if(start > -1 && end > -1) {
            String goodsData = data.substring(start, end);
            goodsData = goodsData.replaceAll("###start_goods_content###","");
            StringBuffer stringBuffer = new StringBuffer();
            if(orderGoodsList.size() >= 0) {
                for (OrderGoods orderGoods : orderGoodsList) {
                    Goods goods = JSON.parseObject(orderGoods.getGoodsStr(), Goods.class);
                    String goodsDataReplace = goodsData.replaceAll("###goods_name###", goods.getName());
                    goodsDataReplace = goodsDataReplace.replaceAll("###goods_sell_price###", "" + (Double.parseDouble(""+orderGoods.getGoods().getSell_price()) / 100));
                    if(isAdd) {
                        goodsDataReplace = goodsDataReplace.replaceAll("###goods_count###", "" + (orderGoods.getCount() - orderGoods.getMakingCount()));
                    }else{
                        goodsDataReplace = goodsDataReplace.replaceAll("###goods_count###", "" + orderGoods.getCount());
                    }
                    goodsDataReplace = goodsDataReplace.replaceAll("###goods_total_price###", "" + ((Double.parseDouble(""+orderGoods.getRealPrice())) / 100));

                    stringBuffer.append("###br###");//换行
                    stringBuffer.append(goodsDataReplace);
                }
            }
            data = data.replaceAll(goodsData,stringBuffer.toString());

            data = data.replaceAll("###start_goods_content###", "");
            data = data.replaceAll("###end_goods_content###", "");
        }
        for (OrderGoods orderGoods : orderGoodsList) {//菜品出餐打印
            data = data.replaceAll("###goods_name###", orderGoods.getGoodsName());
            data = data.replaceAll("###goods_sell_price###", "" + (Double.parseDouble("" + orderGoods.getGoods().getSell_price()) / 100));
            if(isAdd) {//增加菜品
                data = data.replaceAll("###goods_count###", "" + (orderGoods.getCount() - orderGoods.getMakingCount()));
            }else{
                data = data.replaceAll("###goods_count###", "" + orderGoods.getCount());
            }
            data = data.replaceAll("###goods_total_price###", "" + ((Double.parseDouble("" + orderGoods.getRealPrice())) / 100));

        }
        data = data.replaceAll("###order_no###",orders.getOrderNo());
        data = data.replaceAll("###total_price###",""+(Double.parseDouble(""+orders.getTotalPrice()) / 100));
        data = data.replaceAll("###real_price###",""+(Double.parseDouble(""+orders.getRealPrice()) / 100));
        data = data.replaceAll("###table_number###",""+orders.getTableNumber());
        data = data.replaceAll("###note###",""+orders.getNote());
        return data;
    }
}

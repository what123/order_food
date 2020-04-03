package com.food.order.plugins.printer;

import com.food.order.model.entity.Printer;
import com.food.order.plugins.PluginsData;

import java.util.List;
import java.util.Map;

/**
 * 打印机接口
 */
public interface PrinterService {

    public PluginsData addPrinter(Printer printer);

    public PluginsData delPrinter(Printer printer);

    public String getPrintData(String data);//获取转换的打印内容

    public PluginsData printData(Printer printer);//打印

    public PluginsData updatePrinter(Printer printer);//修改打印机

    public PluginsData clearPrintDataSqs(Printer printer);//清空打印队列

    public PluginsData quertPrintData(Printer printer);//查询订单打印的状态

    public PluginsData quertPrinterStatus(Printer printer);//查询打印机状态



}

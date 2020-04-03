package com.food.order.utils.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogUtil {

	/**
	 * 日志级别说明：
	 *  trace  跟踪程序执行时使用
	 *  debug  调试程序时使用
	 *  info   业务日志信息
	 *  warn   不影响程序运行的错误信息
	 *  error   程序处于运行状态，但影响正常使用
	 *  fail    程序即将终止
	 */

	/**
	 * 日志按业务分类
	 *   systemlog   系统日志：启动、
	 *
	 */


	/**
	 * 系统日志   系统启动、初始化
	 */
	public static final Logger systemLog = LoggerFactory.getLogger("SystemLogger");//

	/**
	 *  入口日志 记录有统一入口的 请求与返回
	 */
	public static final Logger accessLog = LoggerFactory.getLogger("AccessLogger");//


	/**
	 * 业务日志  异常处理日志等
	 */
	public static final Logger infoLog = LoggerFactory.getLogger("InfoLogger");//


	/**
	 * 业务日志  异常处理日志等
	 */
	public static final Logger monitorLog = LoggerFactory.getLogger("MonitorLogger");//

	
}

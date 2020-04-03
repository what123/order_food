package com.food.order.utils.utils;
import java.io.*;
import java.util.Properties;


/**
 * @Description: Java读写修改Property文件
 * @author: zhousp
 * @date:   2016年11月18日 下午3:00:46
 */
public class PropertiesUtil {  
	//定义静态全局属性
	private static Properties prop = null;
	
	//定义静态全局属性
	private static Properties desktopPolicyProp = null;
		
	
    /** 
     * 根据KEY，读取文件对应的值 
     * @param filePath 文件路径，即文件所在包的路径，例如：java/util/config.properties 
     * @param key 键 
     * @return key对应的值 
     */  
    public static String readData(String filePath, String key) {  
        //获取绝对路径  
        filePath = PropertiesUtil.class.getResource("/" + filePath).toString();  
        //截掉路径的”file:“前缀  
        filePath = filePath.substring(6);  
        Properties props = new Properties();  
        try {  
            InputStream in = new BufferedInputStream(new FileInputStream(filePath));  
            props.load(in);  
            in.close();  
            String value = props.getProperty(key);  
            return value;  
        } catch (Exception e) {
            e.printStackTrace();  
            return null;  
        }  
    }  
    /** 
     * 修改或添加键值对 如果key存在，修改, 反之，添加。 
     * @param filePath 文件路径，即文件所在包的路径，例如：java/util/config.properties 
     * @param key 键 
     * @param value 键对应的值 
     */  
    public static void writeData(String filePath, String key, String value) {  
        //获取绝对路径  
        filePath = PropertiesUtil.class.getResource("/" + filePath).toString();  
        //截掉路径的”file:/“前缀  
        filePath = filePath.substring(6);  
        Properties prop = new Properties();  
        try {  
            File file = new File(filePath);  
            if (!file.exists())  
                file.createNewFile();  
            InputStream fis = new FileInputStream(file);  
            prop.load(fis);  
            //一定要在修改值之前关闭fis  
            fis.close();  
            OutputStream fos = new FileOutputStream(filePath);  
            prop.setProperty(key, value);  
            //保存，并加入注释  
            prop.store(fos, "Update '" + key + "' value");  
            fos.close();  
        } catch (IOException e) {  
            System.err.println("Visit " + filePath + " for updating " + value + " value error");  
        }  
    }  
      
    /**
     * 读取properties配置文件中值
     */
    public static String readProperty(String key){
    	if (prop == null) {
    		init();
    	}
        String re = prop.getProperty(key);
        return re==null?null:re.trim();
    }
    
    /**
	 * 读取配置信息，初始化prop
	 */
	private static void init() {
		prop = new Properties();
		InputStream in = null;
    	String path = "/application.properties";
    	 try {  
           in = PropertiesUtil.class.getResourceAsStream(path);
           prop.load(in);  
       } catch (Exception e) {  
           e.printStackTrace();  
       } finally {
    	   if (in != null) {
    		   try {
    			   in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
    	   }
       }
	}
	
	/**
	 * 读取配置信息，初始化prop
	 */
	private static void initDesktopPolicy() {
		desktopPolicyProp = new Properties();
		InputStream in = null;
		String path = "/desktopPolicy.properties";
		try {
			in = PropertiesUtil.class.getResourceAsStream(path);
			desktopPolicyProp.load(new InputStreamReader(in,"UTF-8"));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
     * 读取properties配置文件中值
     */
    public static String readDesktopPolicyProp(String key){
//    	if (desktopPolicyProp == null) {
			initDesktopPolicy();
//    	}
        String re = desktopPolicyProp.getProperty(key);
    	return re==null?null:re.trim();
    }


}  
 
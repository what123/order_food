package com.food.order.utils.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * md5不可逆加密，用于加密用户密码
 * 
 * @author 
 * 
 */
public class MD5Util {
	private static Logger logger = LoggerFactory.getLogger(MD5Util.class);
    /** */
    /**
     * The hex digits.
     */
    private static final String[] hexDigits = { "0", "1", "2", "3", "4", "5",
            "6", "7", "8", "9", "a", "b", "c", "d", "e", "f" };

    /** */
    /**
     * Transform the byte array to hex string.
     * 
     * @param b
     * @return
     */
    public static String byteArrayToHexString(byte[] b) {
        StringBuffer resultSb = new StringBuffer();
        for (int i = 0; i < b.length; i++) {
            resultSb.append(byteToHexString(b[i]));
        }
        return resultSb.toString();
    }

    /** */
    /**
     * Transform a byte to hex string.
     * 
     * @param b
     * @return
     */
    private static String byteToHexString(byte b) {
        int n = b;
        if (n < 0)
            n = 256 + n;

        // get the first four bit
        int d1 = n / 16;

        // get the second four bit
        int d2 = n % 16;

        return hexDigits[d1] + hexDigits[d2];
    }

    /** */
    /**
     * Get the MD5 encrypt hex string of the origin string. <br/>
     * The origin string won't validate here, so who use the API should validate
     * by himself.
     * 
     * @param origin
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static String MD5Encode(String origin)
            throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        return byteArrayToHexString(md.digest(origin.getBytes()));
    }

    public static String MD5Encode(byte[] origin)
            throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        return byteArrayToHexString(md.digest(origin));
    }
    
    
    /**
	 * 适用于上G大的文件
	 */
	public static String md5File(String path) throws OutOfMemoryError,
			IOException {
		File file = new File(path);
		FileInputStream in = new FileInputStream(file);
		MessageDigest messagedigest;
		try {
			messagedigest = MessageDigest.getInstance("MD5");
			byte[] buffer = new byte[1024 * 256];
			int len = 0;
			while ((len = in.read(buffer)) > 0) {
				// 该对象通过使用 update（）方法处理数据
				messagedigest.update(buffer, 0, len);
			}

			// 对于给定数量的更新数据，digest 方法只能被调用一次。在调用 digest 之后，MessageDigest
			// 对象被重新设置成其初始状态。
			return byte2hexString(messagedigest.digest());
		} catch (NoSuchAlgorithmException e) {
			logger.error(e.getMessage());
		} catch (OutOfMemoryError e) {
			logger.error(e.getMessage());
			throw e;
		} finally {
			in.close();
		}
		return null;
	}

	public static final String byte2hexString(byte[] bytes) {
		StringBuffer buf = new StringBuffer(bytes.length * 2);
		int len = bytes.length;
		for (int i = 0; i < len; i++) {
			if (((int) bytes[i] & 0xff) < 0x10) {
				buf.append("0");
			}
			buf.append(Long.toString((int) bytes[i] & 0xff, 16));
		}
		return buf.toString();
	}
}

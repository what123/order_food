package com.food.order.utils.utils;



import org.apache.tomcat.util.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.io.IOException;
import java.security.SecureRandom;

/**
 * 对字符串的加密解密工具类
 * 
 * @author 谭小勇
 * 
 */
public class DESUtil {
	private byte[] desKey;
	public final String DES_KEY = "windesktop*&65key365@!set";// 数据加密密钥
	public DESUtil(String desKey) {
		this.desKey = desKey.getBytes();
	}

	public DESUtil() {
		this.desKey = DES_KEY.getBytes();// 使用默认密钥
	}

	private byte[] desEncrypt(byte[] plainText) throws Exception {
		SecureRandom sr = new SecureRandom();
		byte rawKeyData[] = desKey;
		DESKeySpec dks = new DESKeySpec(rawKeyData);
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
		SecretKey key = keyFactory.generateSecret(dks);
		Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, key, sr);
		byte data[] = plainText;
		byte encryptedData[] = cipher.doFinal(data);
		return encryptedData;
	}

	private byte[] desDecrypt(byte[] encryptText) throws Exception {
		SecureRandom sr = new SecureRandom();
		byte rawKeyData[] = desKey;
		DESKeySpec dks = new DESKeySpec(rawKeyData);
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
		SecretKey key = keyFactory.generateSecret(dks);
		Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, key, sr);
		byte encryptedData[] = encryptText;
		byte decryptedData[] = cipher.doFinal(encryptedData);
		return decryptedData;
	}

	/**
	 * 加密
	 * 
	 * @param input
	 * @return
	 * @throws Exception
	 */
	public String encrypt(String input) throws Exception {
		input = input==null? "":input;
		return base64Encode(desEncrypt(input.getBytes()));
	}

	/**
	 * 解密
	 * 
	 * @param input
	 * @return
	 * @throws Exception
	 */
	public String decrypt(String input) throws Exception {
		input = input==null?"":input;
		byte[] result = base64Decode(input);
		return new String(desDecrypt(result));
	}

	private String base64Encode(byte[] s) {
		if (s == null)
			return null;
		// BASE64Encoder b = new BASE64Encoder();
		return Base64.encodeBase64String(s);
		// return b.encode(s);
		// return "";
	}

	private byte[] base64Decode(String s) throws IOException {
		if (s == null)
			return null;
		byte[] b = Base64.decodeBase64(s);
		// BASE64Decoder decoder = new BASE64Decoder();
		// byte[] b = decoder.decodeBuffer(s);
		return b;
		// return null;
	}

	public static void main(String[] args) {
		DESUtil d = new DESUtil("178277164@qq.com");
		try {
//			System.out.println(d.encrypt(null));
//			System.out.println(d.encrypt(""));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
	}
}

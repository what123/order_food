//package com.food.order.utils.utils.rsa;
//
//
//
//import org.apache.commons.codec.binary.Base64;
//import org.apache.commons.io.IOUtils;
//import org.json.JSONObject;
//
//import javax.crypto.Cipher;
//import java.io.ByteArrayOutputStream;
//import java.security.*;
//import java.security.interfaces.RSAPrivateKey;
//import java.security.interfaces.RSAPublicKey;
//import java.security.spec.InvalidKeySpecException;
//import java.security.spec.PKCS8EncodedKeySpec;
//import java.security.spec.X509EncodedKeySpec;
//import java.util.HashMap;
//import java.util.Map;
//
///**
// * @ClassName: RSAUtils
// * @Description: TODO(这里用一句话描述这个类的作用)
// * @author chenkun
// * @date 2019年3月25日
// * http://www.flyinghorse.org.cn/
// */
//public class RSA2 {
//    public static String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCCF3p1vXS11jv5beQTJ5xowGzWwndmkLwQ4OwG\n" +
//            "ZT1ebbJJlcAbHizk87a3A48Dk5u34yBTt35fDkr8ajtu5wCGe+1/iO5GtgX2UviA9VVfn6CAUvZe\n" +
//            "LdzmQdNa++Qe0LQqjHczZS4G28Gg8gTUluXYzTmwAW17IisREGZEcfHbgQIDAQAB";
//    public static String privateKey = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAIIXenW9dLXWO/lt5BMnnGjAbNbC\n" +
//            "d2aQvBDg7AZlPV5tskmVwBseLOTztrcDjwOTm7fjIFO3fl8OSvxqO27nAIZ77X+I7ka2BfZS+ID1\n" +
//            "VV+foIBS9l4t3OZB01r75B7QtCqMdzNlLgbbwaDyBNSW5djNObABbXsiKxEQZkRx8duBAgMBAAEC\n" +
//            "gYAunrdWBmUS3Ql1I7p+Ws2khID8nYgoi2m6KsCqshFCC9G+qRnFHhU8n1I1V+Mihv1g8tYc6j8x\n" +
//            "VW/t8SwHXGVFSY7qxPsdkH+Iwcy4p0bEpu9vN3iUI2kRkhDYajsZ3NQK7jGBJPas47v4aW6WGASv\n" +
//            "jphbZ15Hef+HEfkmZb3AgQJBAMHQ0jhLPX+8x8FvlcI2OvBkRw/1zGPECxP9CZwg0ojd/qYDStXh\n" +
//            "BXIV0HMoUCaUIRCDZQvqzVqbhkCwMIxDLRkCQQCr1KVAASnUU1O+wRI43IXJMshTo1WOOHSvEHnC\n" +
//            "Fk/sCXK/lAcCc5fwV/KIPxTQpVkWo5EBkcb0rR0NfZxxroapAkAXdSq8XX7oAvU6WEML6nxftANV\n" +
//            "zy4ZtRSSKsIK/337ysKTXTqlgEKkeDdueKaaxLrrwLtuIdEQ8lnhYnG9yDOhAkAJkTh2PNpYYR9b\n" +
//            "kqJyGg0066ftyD1eQtR0XuV4ogdnLAuAkTxmrYr7LdpqLaD6EQrJ0Oek/7nlK7JfauwIBGCpAkEA\n" +
//            "iagr4iqZBNEH24nhejnslwQaUPe8ixoldrk3m0eMN2YvuUC7sBMGyjltwzqu6ZAYaEmnVwM9O5DB\n" +
//            "9BRwCcYjTg==";
//    public static final String CHARSET = "UTF-8";
//    public static final String RSA_ALGORITHM = "RSA";
//
//    public static Map<String, String> createKeys(int keySize) {
//        // 为RSA算法创建一个KeyPairGenerator对象
//        KeyPairGenerator kpg;
//        try {
//            kpg = KeyPairGenerator.getInstance(RSA_ALGORITHM);
//        } catch (NoSuchAlgorithmException e) {
//            throw new IllegalArgumentException("No such algorithm-->[" + RSA_ALGORITHM + "]");
//        }
//        // 初始化KeyPairGenerator对象,密钥长度
//        kpg.initialize(keySize);
//        // 生成密匙对
//        KeyPair keyPair = kpg.generateKeyPair();
//        // 得到公钥
//        Key publicKey = keyPair.getPublic();
//        String publicKeyStr = Base64.encodeBase64String(publicKey.getEncoded());
//        // 得到私钥
//        Key privateKey = keyPair.getPrivate();
//        String privateKeyStr = Base64.encodeBase64String(privateKey.getEncoded());
//        Map<String, String> keyPairMap = new HashMap<String, String>();
//        keyPairMap.put("publicKey", publicKeyStr);
//        keyPairMap.put("privateKey", privateKeyStr);
//        return keyPairMap;
//    }
//    /**
//     * 得到公钥
//     * @throws Exception
//     */
//    public static RSAPublicKey getPublicKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
//        // 通过X509编码的Key指令获得公钥对象
//        KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
//        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(Base64.decodeBase64((publicKey)));
//        RSAPublicKey key = (RSAPublicKey) keyFactory.generatePublic(x509KeySpec);
//        return key;
//    }
//    /**
//     * 得到私钥
//     * @throws Exception
//     */
//    public static RSAPrivateKey getPrivateKey()
//            throws NoSuchAlgorithmException, InvalidKeySpecException {
//        // 通过PKCS#8编码的Key指令获得私钥对象
//        KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
//        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(Base64.decodeBase64(privateKey));
//        RSAPrivateKey key = (RSAPrivateKey) keyFactory.generatePrivate(pkcs8KeySpec);
//        return key;
//    }
//
//    /**
//     * 公钥加密
//     * @param data
//     * @return
//     */
//    public static String publicEncrypt(String data) {
//        try {
//            RSAPublicKey publicKey = getPublicKey();
//            Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
//            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
//            return Base64.encodeBase64String(rsaSplitCodec(cipher, Cipher.ENCRYPT_MODE, data.getBytes(CHARSET),
//                    publicKey.getModulus().bitLength()));
//        } catch (Exception e) {
//            throw new RuntimeException("加密字符串[" + data + "]时遇到异常", e);
//        }
//    }
//    /**
//     * 私钥解密
//     * @param data
//     * @return
//     */
//    public static String privateDecrypt(String data) {
//        try {
//            RSAPrivateKey privateKey = getPrivateKey();
//            Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
//            cipher.init(Cipher.DECRYPT_MODE, privateKey);
//            return new String(rsaSplitCodec(cipher, Cipher.DECRYPT_MODE, Base64.decodeBase64(data),
//                    privateKey.getModulus().bitLength()), CHARSET);
//        } catch (Exception e) {
//            throw new RuntimeException("解密字符串[" + data + "]时遇到异常", e);
//        }
//    }
//    /**
//     * 私钥加密
//     * @param data
//     * @return
//     */
//    public static String privateEncrypt(String data) {
//        try {
//            RSAPrivateKey privateKey = getPrivateKey();
//            Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
//            cipher.init(Cipher.ENCRYPT_MODE, privateKey);
//            return Base64.encodeBase64String(rsaSplitCodec(cipher, Cipher.ENCRYPT_MODE, data.getBytes(CHARSET),
//                    privateKey.getModulus().bitLength()));
//        } catch (Exception e) {
//            throw new RuntimeException("加密字符串[" + data + "]时遇到异常", e);
//        }
//    }
//    /**
//     * 公钥解密
//     * @param data
//     * @return
//     */
//    public static String publicDecrypt(String data) {
//        try {
//            RSAPublicKey publicKey = getPublicKey();
//            Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
//            cipher.init(Cipher.DECRYPT_MODE, publicKey);
//            return new String(rsaSplitCodec(cipher, Cipher.DECRYPT_MODE, Base64.decodeBase64(data),
//                    publicKey.getModulus().bitLength()), CHARSET);
//        } catch (Exception e) {
//            throw new RuntimeException("解密字符串[" + data + "]时遇到异常", e);
//        }
//    }
//    private static byte[] rsaSplitCodec(Cipher cipher, int opmode, byte[] datas, int keySize) {
//        int maxBlock = 0;
//        if (opmode == Cipher.DECRYPT_MODE) {
//            maxBlock = keySize / 8;
//        } else {
//            maxBlock = keySize / 8 - 11;
//        }
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//        int offSet = 0;
//        byte[] buff;
//        int i = 0;
//        try {
//            while (datas.length > offSet) {
//                if (datas.length - offSet > maxBlock) {
//                    buff = cipher.doFinal(datas, offSet, maxBlock);
//                } else {
//                    buff = cipher.doFinal(datas, offSet, datas.length - offSet);
//                }
//                out.write(buff, 0, buff.length);
//                i++;
//                offSet = i * maxBlock;
//            }
//        } catch (Exception e) {
//            throw new RuntimeException("加解密阀值为[" + maxBlock + "]的数据时发生异常", e);
//        }
//        byte[] resultDatas = out.toByteArray();
//        IOUtils.closeQuietly(out);
//        return resultDatas;
//    }
//
//    public static void main(String[] args) {
//        createKeys(1024);
//        JSONObject jsonObject = new JSONObject();
//        jsonObject.put("host","http://test.fangwei6.com");
//        jsonObject.put("payjsMchid","1562138301");
//        jsonObject.put("payjsKey","PrSRSV6XRlF6vrdr");
//        jsonObject.put("timeStamp",System.currentTimeMillis());
//        jsonObject.put("expireDay",365*100);
//
//        String a = jsonObject.toString();
//        String b = privateEncrypt(a);
//        System.out.println("激活码为："+b);
//
//
//        System.out.println(publicDecrypt(b));
//    }
//}
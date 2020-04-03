package com.food.order.utils.utils.rsahttps;
import com.google.common.io.Resources;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.security.KeyFactory;
public class PublicKeyReader {
    public static PublicKey get() throws Exception {

        String filename = Resources.getResource("publicKeyFile").getPath();
        File f = new File(filename);
        FileInputStream fis = new FileInputStream(f);
        DataInputStream dis = new DataInputStream(fis);
        byte[] keyBytes = new byte[(int)f.length()];
        dis.readFully(keyBytes);
        dis.close();
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }
}

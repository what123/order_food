package com.food.order.utils.utils.rsahttps;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

public class HttpUtil {
    public String httpGet(String url){
        //httpClient
        HttpClient httpClient = new DefaultHttpClient();
        // get method
        HttpGet httpGet = new HttpGet(url);
        // set header
//        String Au="Bearer "+access_token;
//        httpGet.setHeader("Authorization",Au);

        //response
        HttpResponse response = null;
        try{
            response = httpClient.execute(httpGet);
        }catch (Exception e) {}
        //get response into String
        String temp="";
        try{
            HttpEntity entity = response.getEntity();
            temp= EntityUtils.toString(entity,"UTF-8");
        }catch (Exception e) {}
        return temp;
    }
}

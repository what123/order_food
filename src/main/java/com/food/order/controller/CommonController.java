package com.food.order.controller;


import com.food.order.model.MsgVo;
import com.food.order.model.entity.Goods;
import com.food.order.model.entity.MainStore;
import com.food.order.model.entity.UploadFile;
import com.food.order.model.entity.User;
import com.food.order.model.repository.PaymentsConfigRepository;
import com.github.binarywang.utils.qrcode.MatrixToImageWriter;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.food.order.model.repository.UploadFileRepository;
import com.food.order.utils.utils.FileMd5Utils;
import com.food.order.utils.utils.ImageUploadUtil;
import com.food.order.utils.utils.LogUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * Created by Administrator on 2018/4/2.
 */
@Api( value = "公用接口", description = "公用的接口",tags="公共-公用接口")
@RestController("common")
@RequestMapping("/api/common")
@CrossOrigin
public class CommonController extends BaseController {

    @Value("${site.upload_path}")
    private  String upload_path;

    @Autowired
    UploadFileRepository uploadFileRepository;

    @Autowired
    PaymentsConfigRepository paymentsConfigRepository;

    /**
     * 获得二维码
     * @param request
     * @param response
     */
    @RequestMapping(value = "/qrCode",method={RequestMethod.POST,RequestMethod.GET})
    public void getQrCode(HttpServletRequest request, HttpServletResponse response,
                          @RequestParam("content")String content,
                          @RequestParam(value = "width",defaultValue = "200")int width,
                          @RequestParam(value = "height",defaultValue = "200")int height){


        try {
            createdQrCode(URLDecoder.decode(content,"UTF-8"), response.getOutputStream(), width, height);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    /**
     *  生成web版本二维码
     * @param width 二维码宽度
     * @param height 二维码高度
     * @throws IOException
     */
    private void createdQrCode(String content, ServletOutputStream stream, int width, int height) throws IOException {
        if (content != null && !"".equals(content)) {
            try {
                QRCodeWriter writer = new QRCodeWriter();
                BitMatrix m = writer.encode(content, BarcodeFormat.QR_CODE, height, width);
                MatrixToImageWriter.writeToStream(m, "png", stream);
            } catch (WriterException e) {
                e.printStackTrace();
                LogUtil.infoLog.error("生成二维码失败!");
            } finally {
                if (stream != null) {
                    stream.flush();
                    stream.close();
                }
            }
        }
    }

    @RequestMapping(value = "/imageUpload",method={RequestMethod.POST})
    public void imageUpload(HttpServletRequest request, HttpServletResponse response,@RequestParam(value = "key",defaultValue = "") String key) {
        //String DirectoryName = "D:/test/";
        try {

            String fileName = ImageUploadUtil.ckeditor(request, response, upload_path);

            //String imageContextPath =  "/common/download/pic?fileName="+ URLEncoder.encode(upload_path + "/" + fileName,"utf-8");
            String imageContextPath =  "/common/download/pic/"+ URLEncoder.encode(fileName,"utf-8");
            File file = new File(upload_path + "/" + fileName);
            String md5 = FileMd5Utils.getFileMD5String(file);
            UploadFile uploadFile = uploadFileRepository.findOneByMd5(md5);
            if(uploadFile == null) {
                String ext = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
                uploadFile = new UploadFile();
                uploadFile.setName(fileName);
                uploadFile.setExt(ext);
                uploadFile.setMd5(md5);
                uploadFile.setUrl(imageContextPath);
                uploadFile.setPointCount(1);
                uploadFile.setSize(file.length());
                if (ImageUploadUtil.imgFileTypes.contains(ext)){
                    uploadFile.setThumbnail(imageContextPath);
                    uploadFile.setType("img");
                }else if(ImageUploadUtil.officeFileTypes.contains(ext)){
                    // 文件的缩略图
                    uploadFile.setThumbnail("/common/download/file");
                    uploadFile.setType("office");
                }else  if (ImageUploadUtil.fileTypes.contains(ext)){
                    uploadFile.setThumbnail("/common/download/file");
                    uploadFile.setType("file");
                }

            }else{
                file.delete();//删除这个文件，使用旧文件
                uploadFile.setPointCount(uploadFile.getPointCount()+1);
            }
            uploadFileRepository.save(uploadFile);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("errno",0);

            JSONArray fileNamesJson = new JSONArray();
            fileNamesJson.put(upload_path + "/" + fileName);
            jsonObject.put("paths",fileNamesJson);

            JSONArray jsonArray = new JSONArray();
            jsonArray.put(imageContextPath);
            jsonObject.put("data",jsonArray);
            JSONArray idsArray = new JSONArray();
            idsArray.put(uploadFile.getId());
            jsonObject.put("ids",idsArray);
            idsArray.put(uploadFile.getExt());
            jsonObject.put("exts",idsArray);
            JSONArray thumbnailArray = new JSONArray();
            thumbnailArray.put(uploadFile.getThumbnail());
            jsonObject.put("thumbnails",thumbnailArray);
            jsonObject.put("key",key);
            response.setContentType("text/html;charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.println(jsonObject.toString());
            out.flush();
            out.close();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    /**
     * 通过url请求返回图像的字节流
     */
    @RequestMapping(value ="/download/pic",method={RequestMethod.GET})
    public void getPic(@RequestParam("fileName") String fileName,
                       HttpServletRequest request,
                       HttpServletResponse response) throws IOException {

        if(StringUtils.isEmpty(fileName)) {
            fileName = "";
            return;
        }
        fileName = URLDecoder.decode(fileName,"utf-8");
//        String fileName = request.getSession().getServletContext().getRealPath("/")
//                + "resource\\is\\auth\\"
//                + pic.toUpperCase().trim() + ".png";
        File file = new File(upload_path + "/"+fileName);

        //判断文件是否存在如果不存在就返回默认图标
        if(!(file.exists() && file.canRead())) {
            file = new File(request.getSession().getServletContext().getRealPath("/")
                    + "resource/icons/auth/root.png");
        }

        FileInputStream inputStream = new FileInputStream(file);
        byte[] data = new byte[(int)file.length()];
        int length = inputStream.read(data);
        inputStream.close();

        response.setContentType("image/png");

        OutputStream stream = response.getOutputStream();
        stream.write(data);
        stream.flush();
        stream.close();
    }
    /**
     * 通过url请求返回图像的字节流
     */
    @RequestMapping(value ="/download/pic/{fileName}",method={RequestMethod.GET})
    public void getPicPath(@PathVariable("fileName") String fileName,
                       HttpServletRequest request,
                       HttpServletResponse response) throws IOException {

        if(StringUtils.isEmpty(fileName)) {
            fileName = "";
            return;
        }
        fileName = URLDecoder.decode(fileName,"utf-8");
//        String fileName = request.getSession().getServletContext().getRealPath("/")
//                + "resource\\is\\auth\\"
//                + pic.toUpperCase().trim() + ".png";
        File file = new File(upload_path+"/"+fileName);

        //判断文件是否存在如果不存在就返回默认图标
        if(!(file.exists() && file.canRead())) {
            file = new File(request.getSession().getServletContext().getRealPath("/")
                    + "resource/icons/auth/root.png");
        }

        FileInputStream inputStream = new FileInputStream(file);
        byte[] data = new byte[(int)file.length()];
        int length = inputStream.read(data);
        inputStream.close();

        response.setContentType("image/png");

        OutputStream stream = response.getOutputStream();
        stream.write(data);
        stream.flush();
        stream.close();
    }
    /**
     * 通过url请求返回图像的字节流
     */
    @RequestMapping(value ="/download/logo",method={RequestMethod.GET})
    public void logo(HttpServletRequest request,
                       HttpServletResponse response) throws IOException {

        File file = ResourceUtils.getFile("classpath:static/images/logo.jpg");

        //判断文件是否存在如果不存在就返回默认图标
        if(!(file.exists() && file.canRead())) {
            file = new File(request.getSession().getServletContext().getRealPath("/")
                    + "resource/icons/auth/root.png");
        }

        FileInputStream inputStream = new FileInputStream(file);
        byte[] data = new byte[(int)file.length()];
        int length = inputStream.read(data);
        inputStream.close();

        response.setContentType("image/jpg");

        OutputStream stream = response.getOutputStream();
        stream.write(data);
        stream.flush();
        stream.close();
    }
    /**
     * 通过url请求返回图像的字节流
     */
    @RequestMapping(value ="/download/file",method={RequestMethod.GET})
    public void fileicon(HttpServletRequest request,
                     HttpServletResponse response) throws IOException {

        File file = ResourceUtils.getFile("classpath:static/images/file.png");

        //判断文件是否存在如果不存在就返回默认图标
        if(!(file.exists() && file.canRead())) {
            file = new File(request.getSession().getServletContext().getRealPath("/")
                    + "resource/icons/auth/root.png");
        }

        FileInputStream inputStream = new FileInputStream(file);
        byte[] data = new byte[(int)file.length()];
        int length = inputStream.read(data);
        inputStream.close();

        response.setContentType("image/jpg");

        OutputStream stream = response.getOutputStream();
        stream.write(data);
        stream.flush();
        stream.close();
    }




}

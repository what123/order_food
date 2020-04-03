package com.food.order.utils.utils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * @ClassName: ImageUploadUtil
 * @Description: 图片上传工具类，包括ckeditor操作
 */
public class ImageUploadUtil {

    // 类型
    public static List<String> fileTypes = new ArrayList<String>();
    public static List<String> imgFileTypes = new ArrayList<String>();
    public static List<String> officeFileTypes = new ArrayList<String>();
    static {
        fileTypes.add(".p12");
        imgFileTypes.add(".jpg");
        imgFileTypes.add(".jpeg");
        imgFileTypes.add(".bmp");
        imgFileTypes.add(".gif");
        imgFileTypes.add(".png");
        officeFileTypes.add(".pdf");
        officeFileTypes.add(".doc");
        officeFileTypes.add(".docx");
        officeFileTypes.add(".txt");
        officeFileTypes.add(".xls");
        officeFileTypes.add(".xlsx");
        officeFileTypes.add(".ppt");
        officeFileTypes.add(".pptx");
    }

    /**
     * 图片上传
     *
     * @Title upload
     * @param request
     *            文件上传目录：比如upload(无需带前面的/) upload/news ..
     * @return
     * @throws IllegalStateException
     * @throws IOException
     */
    public static String upload(HttpServletRequest request, String upgrade_bin) throws IllegalStateException,
            IOException {
        // 创建一个通用的多部分解析器
        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(request.getSession()
                .getServletContext());
        // 图片名称
        String fileName = null;
        // 判断 request 是否有文件上传,即多部分请求
        if (multipartResolver.isMultipart(request)) {
            // 转换成多部分request
            MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
            // 取得request中的所有文件名
            Iterator<String> iter = multiRequest.getFileNames();
            while (iter.hasNext()) {
                // 记录上传过程起始时的时间，用来计算上传时间
                // int pre = (int) System.currentTimeMillis();
                // 取得上传文件
                MultipartFile file = multiRequest.getFile(iter.next());
                if (file != null) {
                    // 取得当前上传文件的文件名称
                    String myFileName = file.getOriginalFilename();
                    // 如果名称不为“”,说明该文件存在，否则说明该文件不存在
                    if (myFileName.trim() != "") {
                        // 获得图片的原始名称
                        String originalFilename = file.getOriginalFilename();
                        // 获得图片后缀名称,如果后缀不为图片格式，则不上传
                        String suffix = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
                        if (!imgFileTypes.contains(suffix) && !officeFileTypes.contains(suffix) && !fileTypes.contains(suffix)) {
                            continue;
                        }
                        // 获得上传路径的绝对路径地址(/upload)-->
                        //String realPath = request.getSession().getServletContext().getRealPath("/" + DirectoryName);
                        //String realPath = "D:/test/";
                        System.out.println(upgrade_bin);
                        // 如果路径不存在，则创建该路径
                        File realPathDirectory = new File(upgrade_bin);
                        if (realPathDirectory == null || !realPathDirectory.exists()) {
                            realPathDirectory.mkdirs();
                        }
                        // 重命名上传后的文件名 111112323.jpg
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                        fileName = simpleDateFormat.format(new Date()) + suffix;
                        // 定义上传路径 .../upload/111112323.jpg
                        File uploadFile = new File(realPathDirectory + "/" + fileName);
                        file.transferTo(uploadFile);
                    }
                }
                // 记录上传该文件后的时间
                // int finaltime = (int) System.currentTimeMillis();
                // System.out.println(finaltime - pre);
            }
        }
        return fileName;
    }

    /**
     * ckeditor文件上传功能，回调，传回图片路径，实现预览效果。
     *
     * @Title ckeditor
     * @param request
     * @param response
     * @param DirectoryName
     *            文件上传目录：比如upload(无需带前面的/) upload/..
     * @throws IOException
     */
    public static String ckeditor(HttpServletRequest request, HttpServletResponse response, String DirectoryName)
            throws IOException {
        return upload(request, DirectoryName);



    }
}

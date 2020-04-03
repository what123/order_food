package com.food.order.interceptor;


import com.food.order.model.config.Config;
import com.food.order.model.criteria.Criteria;
import com.food.order.model.criteria.Restrictions;
import com.food.order.model.entity.*;
import com.food.order.model.repository.MainStoreRepository;
import com.food.order.model.repository.StoreRepository;
import com.food.order.model.repository.TmpCacheRepository;
import com.food.order.model.repository.UserRepository;
import com.food.order.utils.utils.PropertiesUtil;
import com.food.order.websocket.WebSocketServer;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 分店管理员登录
 * Created by Administrator on 2017/11/14.
 */
public class StoreAuthInterceptor implements HandlerInterceptor {
    @Autowired
    UserRepository userRepository;
    @Autowired
    StoreRepository storeRepository;
    @Autowired
    TmpCacheRepository tmpCacheRepository;

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {



        String token = request.getHeader("access_token");//授权码
        if(token == null) {
            token = request.getParameter("access_token");//授权码
            if(token == null || StringUtils.isEmpty(token)) {
                errorHandler(response,5001,"access_token无效,请重新登录");
                return false;
            }
        }
        token = token.trim();
//        HttpSession sessoin= request.getSession();//这就是session的创建
//        System.out.println("sessionid22++++++++++++++++++++++++++++++"+sessoin.getId());
        TmpCache tmpCache = tmpCacheRepository.findOneByCKey(token);
        if(tmpCache == null || tmpCache.getEndTime() < System.currentTimeMillis()){
            errorHandler(response,5003,"access_token无效,请重新登录");
            return false;
        }

        User user =  userRepository.findById(Long.parseLong(tmpCache.getCValue())).orElse(null);
        if(user == null || user.isDelete() || user.getBelong() != 3){
            errorHandler(response,5002,"access_token无效,请重新登录");
            return false;
        }
        if(user.getIsFreeze() == 1){
            errorHandler(response,5005,"您的帐号已被冻结，请联系管理员");
            return false;
        }

        // 查询对应的店家数据
        Criteria<Store> criteria = new Criteria<>();
        criteria.add(Restrictions.eq("user",user));
        Store store = storeRepository.findOne(criteria).orElse(null);
        if(store == null){
            errorHandler(response,5004,"access_token无效,请重新登录");
            return false;
        }

        if(store.getMainStore().getUser().getIsFreeze() == 1 || store.getMainStore().getUser().isDelete()){
            errorHandler(response,5006,"总店帐号被冻结或删除");
            return false;
        }

        tmpCache.setEndTime(tmpCache.getEndTime()+tmpCache.getExpireTime());//延长有效期
        tmpCacheRepository.save(tmpCache);
        request.setAttribute("user",store);


        WebSocketServer.authLongTime.put(""+user.getId(),tmpCache.getEndTime());//加入推送连接验证

        return true;
    }

    private void errorHandler(HttpServletResponse response,int code,String msg){
        //重置response
        response.reset();
        //设置编码格式
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Access-Control-Allow-Origin","*");
//        response.setHeader("Access-Control-Allow-Methods","POST, GET, OPTIONS, PUT, PATCH, DELETE");
//        response.setHeader("Access-Control-Allow-Credentials","true");
//        response.setHeader("Access-Control-Allow-Headers","Access-Control-Allow-Headers, Origin,Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Authorization , Access-Control-Request-Headers");
        PrintWriter pw = null;
        try {
            pw = response.getWriter();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("code",code);
            jsonObject.put("msg",msg);
            pw.write(jsonObject.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(pw != null){
                pw.flush();
                pw.close();
            }
        }


    }

    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        //System.out.println(">>>MyInterceptor1>>>>>>>请求处理之后进行调用，但是在视图被渲染之前（Controller方法调用之后）");
    }

    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //System.out.println(">>>MyInterceptor1>>>>>>>在整个请求结束之后被调用，也就是在DispatcherServlet 渲染了对应的视图之后执行（主要是用于进行资源清理工作）");
    }
}

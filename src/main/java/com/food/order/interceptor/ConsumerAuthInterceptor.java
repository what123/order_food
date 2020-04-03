package com.food.order.interceptor;


import com.food.order.model.criteria.Criteria;
import com.food.order.model.criteria.Restrictions;
import com.food.order.model.entity.Consumer;
import com.food.order.model.entity.MainStore;
import com.food.order.model.entity.TmpCache;
import com.food.order.model.entity.User;
import com.food.order.model.repository.ConsumerRepository;
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
import java.util.Enumeration;

/**
 * Created by Administrator on 2017/11/14.
 */
public class ConsumerAuthInterceptor implements HandlerInterceptor {
    @Autowired
    UserRepository userRepository;
    @Autowired
    TmpCacheRepository tmpCacheRepository;
    @Autowired
    ConsumerRepository consumerRepository;

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //System.out.println(">>>MyInterceptor1>>>>>>>在请求处理之前进行调用（Controller方法调用之前）");
        //new DESUtil("13710124580").encrypt(account+"#@#"+user1.getId()+"#@#"+System.currentTimeMillis());

        String token = request.getHeader("access_token");//授权码
        Enumeration<String> es =  request.getHeaderNames();
        if(token == null) {
            token = request.getParameter("access_token");//授权码
            if(token == null || StringUtils.isEmpty(token)) {
                errorHandler(response,5001,"access_token无效,请重新登录");
                return false;
            }
        }
        token = token.trim();
//        HttpSession sessoin= request.getSession();//这就是session的创建
        TmpCache tmpCache = tmpCacheRepository.findOneByCKey(token);
        if(tmpCache == null || tmpCache.getEndTime() < System.currentTimeMillis()){
            errorHandler(response,5003,"access_token无效,请重新登录");
            return false;
        }
        User user =  userRepository.findById(Long.parseLong(tmpCache.getCValue())).orElse(null);
//        Parent parent = (Parent) sessoin.getAttribute(token);
        if(user == null){
            errorHandler(response,5002,"access_token无效,请重新登录");
            return false;
        }

        // 查询对应的店家数据
        Criteria<Consumer> criteria = new Criteria<>();
        criteria.add(Restrictions.eq("user",user));
        Consumer consumer = consumerRepository.findOne(criteria).orElse(null);
        if(consumer == null){
            errorHandler(response,5004,"access_token无效,请重新登录");
            return false;
        }
        tmpCache.setEndTime(tmpCache.getEndTime()+tmpCache.getExpireTime());//延长有效期
        tmpCacheRepository.save(tmpCache);
        request.setAttribute("user",consumer);
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

package com.food.order.interceptor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Created by Administrator on 2017/11/14.
 */
@Configuration
public class MyWebAppConfigurer extends WebMvcConfigurerAdapter {
   // @Bean
   // public HandlerInterceptor getActiveInterceptor(){
       // return new ActiveInterceptor();
  //  }
   @Bean
   public HandlerInterceptor getSuperAdminAuthInterceptor(){
       return new SuperAdminAuthInterceptor();
   }
    @Bean
    public HandlerInterceptor getMainStoreAuthInterceptor(){
        return new MainStoreAuthInterceptor();
    }
    @Bean
    public HandlerInterceptor getStoreAuthInterceptor(){
        return new StoreAuthInterceptor();
    }

    @Bean
    public HandlerInterceptor getStoreUserAuthInterceptor(){
        return new StoreUserAuthInterceptor();
    }

    @Bean
    public HandlerInterceptor getConsumerAuthInterceptor(){
        return new ConsumerAuthInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        // 多个拦截器组成一个拦截器链
        // addPathPatterns 用于添加拦截规则
        // excludePathPatterns 用户排除拦截
        //registry.addInterceptor(getActiveInterceptor()).addPathPatterns("/admin/**").excludePathPatterns("/admin/user/login");
        registry.addInterceptor(getSuperAdminAuthInterceptor()).addPathPatterns("/api/super_admin/**").excludePathPatterns("/api/super_admin/login");
        registry.addInterceptor(getMainStoreAuthInterceptor()).addPathPatterns("/api/main_store/**").excludePathPatterns("/api/main_store/login");
        registry.addInterceptor(getStoreAuthInterceptor()).addPathPatterns("/api/store/**").excludePathPatterns("/api/store/login");
        registry.addInterceptor(getStoreUserAuthInterceptor()).addPathPatterns("/api/store_user/**").excludePathPatterns("/api/store_user/login","/api/store_user/oauth_callback/{oauth_id}");
        registry.addInterceptor(getConsumerAuthInterceptor()).addPathPatterns("/api/consumer/**").excludePathPatterns("/api/consumer/oauth_callback/{oauth_id}");
        super.addInterceptors(registry);


    }

}

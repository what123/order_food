package com.food.order.plugins.pageView.page;

import com.food.order.plugins.*;
import com.food.order.plugins.pageView.PageViewService;
import com.food.order.plugins.pageView.PageViewTarget;
import com.food.order.utils.utils.MD5Util;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * 默认客户点餐页面
 */
@PageViewTarget
public class DefaultPage extends BasePlugins implements PageViewService, PluginsService {

    @Override
    public String getPluginsTag() {
        return DefaultPage.class.getName();
    }

    @Override
    public int getPluginsType() {
        return PluginsTypeEnum.PAGE_VIEW_PLUGINS.getIndex();
    }

    @Override
    public Integer getVip0Price() {
        return 0;
    }

    @Override
    public Integer getVip1Price() {
        return 0;
    }

    @Override
    public Integer getVip2Price() {
        return 0;
    }

    @Override
    public int getExpiryDay() {
        return 0;
    }

    @Override
    public int getFreeExpiryDay() {
        return 0;
    }

    @Override
    public String getName() {
        return "默认";
    }

    @Override
    public String getPicPath() {
        return "/default/preview.png";
    }

    @Override
    public String getNote() {
        return null;
    }

    @Override
    public Map<String, String> getParamsConfig() {
        return null;
    }

    @Override
    public String getPluginsUUID() {
        try {
            return MD5Util.MD5Encode(DefaultPage.class.getName());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public String getViewPath() {
        return "default";
    }
}

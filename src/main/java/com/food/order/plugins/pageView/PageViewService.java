package com.food.order.plugins.pageView;

import com.food.order.model.entity.Orders;
import com.food.order.model.entity.PaymentsConfig;
import com.food.order.plugins.PluginsData;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface PageViewService {

    public String getViewPath();

}

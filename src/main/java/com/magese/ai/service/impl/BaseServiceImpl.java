package com.magese.ai.service.impl;

import com.magese.ai.common.web.PageFilter;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

public class BaseServiceImpl {

    public final static String PAGE_ATTRIBUTE_KEY = "PageFilter";

    /**
     * 获取分页信息
     */
    protected PageFilter getPageFilter() {
        PageFilter pageFilter;
        pageFilter = (PageFilter) RequestContextHolder.currentRequestAttributes().
                getAttribute(PAGE_ATTRIBUTE_KEY, RequestAttributes.SCOPE_REQUEST);
        // 某些页面不需要分页
        // pageFilter = new PageFilter();
        // pageFilter.setStart(0);
        // pageFilter.setLimit(10);
        return pageFilter;
    }
}

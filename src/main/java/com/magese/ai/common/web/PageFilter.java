package com.magese.ai.common.web;

import lombok.Data;

@Data
public class PageFilter {
    private Integer start = 1;

    private Integer limit = 10;

    public PageFilter() {
    }

    public PageFilter(Integer start, Integer limit) {
        this.start = start;
        this.limit = limit;
    }
}

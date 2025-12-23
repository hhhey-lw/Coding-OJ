package com.longoj.top.infrastructure.utils;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;
import java.util.function.Function;

public class PageUtil {

    /**
     * 分页转换，实体类转换
     */
    public static <T, R> Page<R> convertToVO(Page<T> dataPage, Function<T, R> convert) {
        if (dataPage == null) {
            return Page.of(1, 10);
        }
        if (dataPage.getRecords().isEmpty()) {
            return Page.of(dataPage.getPages(), dataPage.getSize(), dataPage.getTotal());
        }

        List<R> voList = dataPage.getRecords().stream().map(convert).toList();
        Page<R> page = new Page<>(dataPage.getPages(), dataPage.getSize(), dataPage.getTotal());
        page.setRecords(voList);
        return page;
    }

}

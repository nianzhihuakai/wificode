package com.nzhk.wificode.common.utils;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class BeanConvertUtil extends BeanUtils {

    public static <S, T> T copySingleProperties(S source, Supplier<T> target) {
        return copySingleProperties(source, target, null);
    }

    public static <S, T> T copySingleProperties(S source, Supplier<T> target, BeanConvertUtilCallBack<S, T> callBack) {
        T t = target.get();
        if (source != null) {
            copyProperties(source, t);
        }
        if (callBack != null) {
            callBack.callBack(source, t);
        }
        return t;
    }

    public static <S, T> List<T> copyListProperties(List<S> sources, Supplier<T> target) {
        return copyListProperties(sources, target, null);
    }

    public static <S, T> List<T> copyListProperties(List<S> sources, Supplier<T> target, BeanConvertUtilCallBack<S, T> callBack) {
        List<T> list = new ArrayList<>(sources != null ? sources.size() : 0);
        if (sources == null) return list;
        for (S source : sources) {
            T t = target.get();
            if (source != null) {
                copyProperties(source, t);
                list.add(t);
            }
            if (callBack != null) {
                callBack.callBack(source, t);
            }
        }
        return list;
    }

    public static <S, T> IPage<T> copyPageProperties(IPage<S> sources, Supplier<T> target) {
        return copyPageProperties(sources, target, null);
    }

    public static <S, T> IPage<T> copyPageProperties(IPage<S> sources, Supplier<T> target, BeanConvertUtilCallBack<S, T> callBack) {
        IPage<T> page = new Page<>();
        page.setRecords(new ArrayList<>());
        if (sources == null) return page;
        copyProperties(sources, page, "records");
        for (S item : sources.getRecords()) {
            T t = target.get();
            copyProperties(item, t);
            page.getRecords().add(t);
            if (callBack != null) callBack.callBack(item, t);
        }
        return page;
    }
}

package com.nzhk.wificode.common.utils;

@FunctionalInterface
public interface BeanConvertUtilCallBack<S, T> {

    void callBack(S s, T t);
}

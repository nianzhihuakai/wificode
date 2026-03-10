package com.nzhk.wificode.common.info;

import lombok.Data;

@Data
public class ResponseInfo<D> {

    private int code;
    private String msg;
    private boolean success;
    private D data;

    public static <T> ResponseInfo<T> success(T data) {
        ResponseInfo<T> info = new ResponseInfo<>();
        info.setCode(200);
        info.setMsg("success");
        info.setSuccess(true);
        info.setData(data);
        return info;
    }

    public static <T> ResponseInfo<T> fail(T data) {
        ResponseInfo<T> info = new ResponseInfo<>();
        info.setCode(10000);
        info.setMsg("fail");
        info.setSuccess(false);
        info.setData(data);
        return info;
    }

    public static <T> ResponseInfo<T> fail(int code, String msg, T data) {
        ResponseInfo<T> info = new ResponseInfo<>();
        info.setCode(code);
        info.setMsg(msg);
        info.setSuccess(false);
        info.setData(data);
        return info;
    }
}

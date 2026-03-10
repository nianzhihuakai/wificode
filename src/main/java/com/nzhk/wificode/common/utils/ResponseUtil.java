package com.nzhk.wificode.common.utils;

import com.alibaba.fastjson.JSONObject;
import com.nzhk.wificode.common.info.ResponseInfo;
import jakarta.servlet.http.HttpServletResponse;

import java.io.PrintWriter;

public class ResponseUtil {

    public static void writeJsonResponse(HttpServletResponse response, int httpStatus, int code, String msg, Object data) {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=utf-8");
        try {
            response.setStatus(httpStatus);
            ResponseInfo<Object> info = ResponseInfo.fail(code, msg, data);
            String json = JSONObject.toJSONString(info);
            PrintWriter out = response.getWriter();
            out.write(json);
        } catch (Exception e) {
            try {
                response.setStatus(httpStatus);
            } catch (Exception ignored) {
            }
        }
    }
}

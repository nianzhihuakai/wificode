package com.nzhk.wificode.common.exception;

import com.nzhk.wificode.common.info.ResponseInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public ResponseInfo<Void> handleBizException(BizException e) {
        log.warn("BizException: code={}, msg={}", e.getCode(), e.getMessage());
        return ResponseInfo.fail(e.getCode(), e.getMessage(), null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseInfo<Void> handleException(Exception e) {
        log.error("Unhandled exception", e);
        return ResponseInfo.fail(50000, "服务器异常", null);
    }
}

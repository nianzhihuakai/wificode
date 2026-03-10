package com.nzhk.wificode.common.info;

import lombok.Data;

import java.io.Serializable;

@Data
public class RequestInfo<T> implements Serializable {

    private T data;
}

package com.cimraffi.android.birdeye.bean.response;

import java.io.Serializable;

/**
 * 云端返回数据的基础结构
 * by baoyh.
 */
public class BaseResponse implements Serializable {
    public String code;
    public String msg;
}

package com.cimraffi.android.birdeye.bean;

import com.cimraffi.android.birdeye.bean.response.BaseResponse;

import java.io.Serializable;
import java.util.ArrayList;

public class DeviceList extends BaseResponse {
    public ArrayList<DeviceData> data;
    public class DeviceData implements Serializable {
        public int id;
        public String name;
        public Integer cameraType;
        public Integer type;
    }
}

package com.cimraffi.android.birdeye.bean;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 航线数据
 */
public class FlyLine implements Serializable {
    public Long         id;
    public String 	    userId;//登录用户
    public String 		name;//航线名称
    public String 		boundaryId;//地块id
    public String         date;//监测日期
    public String 		address;//地址
    public String province; //省
    public String city; //市
    public String district; //县
    public String cityCode; //城市码
    public Integer 	    type;//类型 1航线 2定距 3航点
    public int 		overlap;//垄宽
    public int 		space;//垄宽
    public int 		height;//高度
    public int 		speed;//速度
    public int 		photoInterval;//拍照间隔
    public List<PointBean> points;//航点数组
    public String 		weather;//天气
    public String 	    cropId;//作物id
    public String growthPeriod;//生长期
    public String 	    cropVarietyId;//品种id
    public String 		imageUrl;//图片地址
    public Integer 	    status;//状态 1可用2不可用
    public Integer  source;
    public double   lat;
    public double   lng;
    public List<PointBean> boundary;//边界数组
    public double   acreage;
    public Integer deviceId;//设备id
}

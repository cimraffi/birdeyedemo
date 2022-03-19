package com.cimraffi.android.birdeye.common;

import java.io.File;

/**
 * 全局常量类
 */
public class Constants {
    //登录接口
    public static final String URL_LOGIN = "http://61.167.237.106:8081/oauth/token?client_id=spectrum&client_secret=tgb.258&grant_type=password&scope=user_info&username=%s&password=%s";
    //获取视频流地址接口
    public static final String URL_VIDEO_ADDRESS = "/uav/config?userId=%s";
    public static final String URL_UPDATE_STATUS = "/uav/status/update?userId=%s&customerCode=%s&uavId=%s&status=%s";
    public static final String URL_UPDATE_LOCATE = "/uav/locate/update?userId=%s&customerCode=%s&latitude=%s&longitude=%s";
    //航线接口
    public static final String URL_NEWFLYLINE = "/flyline/saveFlyLine?t=1";
    public static final String URL_UPDATEFLYLINE = "/flyline/updateFlyLine?t=1";
    public static final String URL_UPLOADPHOTO = "/flyline/uploadPhoto?t=1";
    public static final String URL_GETFLYLINE = "/flyline/getFlyLines?userId=%s&type=%d&page=%d&source=%s";
    public static final String URL_GETFLYLINE_WITH_TASK = "/task/getTaskLines?userId=%s&page=%d";
    //勘查任务接口
    public static final String URL_UPLOADTASK =  "/task/uploadTask?code=%s&count=%s" +
            "&surveyType=%s&surveyContent=%s&acreage=%s&boundaryId=%s&monitorTime=%s" +
            "&userId=%s&startTime=%d&endTime=%d" +
            "&wind=%s&temperature=%s&humidity=%s&weatherType=%s";

//    public static final String URL_UPLOADTASKPHOTO = "/fileUpload/multipartfile?t=1";
    public static final String URL_UPLOADTASKPHOTO = "/task/uploadImage?t=1";
    public static final String URL_COMPLATETASK = "/task/completeTask?t=1";
    //地址查询
    public static final String URL_GETADDRESS = "http://api.map.baidu.com/geocoder?location=%s,%s" +
            "&output=json&key=E4805d16520de693a3fe707cdc962045";
    /////////////////////////////////////////////////////////////以下尚未测试
    //更新地址
    public static final String URL_UPDATEADDRESS = "/address/updateAddress?id=%s&address=%s";
    //绑定用户信息
    public static final String URL_BOUNDPOLICY = "/upload/boundPolicy?taskId=%s&policyNumber=%s&caseNumber=%s";
    //作物接口
    public static final String URL_CROPLIST = "/crop/list?t=1";
    public static final String URL_DEVICELIST = "/device/list?type=%s";
    public static final String URL_GROWTHPERIOD_LIST = "/growthperiod/list?t=1&cropId=%s";
    //数据产品接口
    public static final String URL_PRODUCTLIST = "/product/list?t=1";

    /**
     * 获取实时天气+天气预报
     */
    public static final String URL_CURRENT_AND_WEATHER = "https://api.caiyunapp.com/v2/3AN0aEo1OyJzrowF/%s,%s/weather.jsonp?begin=%s";

    public static final int REQUESTCODE_HISTORYAIRWAY = 10;
    public static final int REQUESTCODE_BLUETOOTH = 11;
    public static final int REQUESTCODE_COORTYPE = 15;
    public static final int REQUESTCODE_GODETAIL = 16;
    public static final int REQUESTCODE_HISTORYMONITOR = 17;
    public static final int REQUESTCODE_LOGIN = 18;
    public static final int REQUESTCODE_NEWMISSION = 19;

    public static final int RESULTCODE_DELETE = 10;
    public static final int RESULTCODE_USE = 11;
    public static final int RESULTCODE_EDIT = 12;
    public static final int RESULTCODE_GETAIRWAY = 13;

    public static final int WHAT_PICPATH = 1;
    public static final int WHAT_NOAIRWAY = 2;
    public static final int WHAT_NOPIC = 3;
    public static final int WHAT_SAVESUCCESS = 4;
    public static final int WHAT_SAVEFAILED = 5;
    public static final int WHAT_REFRESH = 6;

    public static final int STATUS_FLYING = 1;
    public static final int STATUS_FALL = 0;
    public static final String PATH_LOG = "monitor" + File.separator +"log";
    /**地图截图存储目录*/
    public static final String PATH_MAPSNAPSHOT = "monitor" + File.separator + "mapsnapshot";
    /**任务照片*/
    public static final String PATH_MISSIONPHOTOS = "monitor" + File.separator + "missionphoto";

    public static final String KEY_USERNAME = "username";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_NAME = "name";
    public static final String KEY_USERID = "userId";
    public static final String KEY_WEATHER = "weather";
    public static final String KEY_MISSIONTYPE = "missionType";
    public static final String KEY_AREAID = "areaId";
    public static final String KEY_AREA = "area";
    public static final String KEY_AREALOG = "areaLog";
    public static final String KEY_CROP = "crop";
    public static final String KEY_FLYLINE = "flyline";
    public static final String KEY_VIDEO_ADDRESS = "videoaddress";
    public static final String KEY_CUSTOMER_CODE = "customercode";
    public static final String KEY_MAP_URL= "mapUrl";
    public static final String KEY_SUBDOMAINS = "subdomains";
    public static final String KEY_TMS = "tms";
    public static final String KEY_ATTRIBUTION = "attribution";

    public static final String CODE_SUCCESS = "10000";
    /**
     * 通知栏
     */
    public static final int NOTIFICATION_ID = 10000;
}

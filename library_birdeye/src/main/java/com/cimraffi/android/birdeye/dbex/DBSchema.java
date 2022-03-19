package com.cimraffi.android.birdeye.dbex;

public class DBSchema {

    public static final class FlyLineTable {
        public static final String NAME = "flyline";

        public static final class cols {
            //航线ID
            public static final String ID ="_id";
            //航线识别码
            public static final String CODE ="code";
            //服务器端航线id
            /*
             * <=0:未存储
             * >0:存储
             * */
            public static final String SVR_FLYLINE_ID = "svr_flyline_id";
            //生成航线的方式
            /*
             * 1:航线
             * 2:随机
             * 3:定距
             * */
            public static final String TYPE = "type";
            //航线生成日期
            public static final String DATE = "date";
            //航线地址
            public static final String ADDRESS = "address";
            //航线详情（JSON格式）
            public static final String JSON = "json";
        }
    }

    public static final class TaskTable {
        public static final String NAME = "task";

        public static final class cols {
            //任务码
            public static final String CODE = "code";
            //任务详情的json
            public static final String JSON = "json";
            //服务器端taskid
            /*
             * <=0:未存储
             * >0:存储
             * */
            public static final String SVR_TASK_ID = "svr_task_id";
            //任务状态
            /*
             * 0:未飞完
             * 1:飞完未上云
             * 2:上云完毕
             * */
            public static final String STATUS = "status";
            //原图状态
            /*
             * 0:未传完
             * 1:已传完
             * */
            public static final String ORIGIN_STATUS = "origin_status";
            //预览图状态
            /*
             * 0:未传完
             * 1:已传完
             * */
            public static final String PREVIEW_STATUS = "preview_status";
        }
    }

    public static final class TaskPhotoTable {
        public static final String NAME = "task_photo";

        public static final class cols {
            //任务ID
            public static final String TASK = "task";
            //照片名称
            public static final String PHOTO = "photo";
            //拍照时间
            public static final String PHOTO_TIME = "photo_time";
            //照片状态
            /*
             * 0:未下载未上传
             * 1:已下载未上传
             * 2:已下载已上传
             * */
            public static final String STATUS = "status";
            //预览图状态
            /*
             * 0:未下载未上传
             * 1:已下载未上传
             * 2:已下载已上传
             * */
            public static final String PREVIEW_STATUS = "preview_status";
            //图片在服务器端的key
            public static final String SVR_KEY = "svr_key";
            //大疆的MEDIAFILE
            public static final String MEDIAFILE = "mediafile";
        }
    }

    public static final class ContinuePointTable {
        public static final String NAME = "continuepoint";

        public static final class cols {
            public static final String _ID = "_id";
            //地块id
            public static final String BOUNDARYID = "boundaryId";
            public static final String MONITORTIME = "monitorTime";
            //任务类型
            public static final String TYPE = "missionType";
            //续航点索引
            public static final String INDEX = "targetWaypointIndex";
            //续航点经纬度
            public static final String LAT = "lat";
            public static final String LNG = "lng";
            //任务起始时间
            public static final String MISSIONSTARTTIME = "missionStartTime";
        }
    }
    /**
     * 日志表
     */
    public static final class AreaLogTable{
        public static final String NAME = "arealog";
        public static final class cols{
            public static final String _ID = "_id";
            public static final String AREAID = "areaID";//地块id
            public static final String CREATETIME = "createtime";//日志创建时间
        }
    }

    public static final class LogsTable{
        public static final String NAME = "logs";
        public static final class cols{
            public static final String _ID = "_id";
            public static final String LOGID = "logId";
            public static final String LOG = "log";//日志
        }
    }
}

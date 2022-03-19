package com.cimraffi.android.birdeye.utils.polygon;

import android.text.TextUtils;

import java.text.DecimalFormat;

/**
 * 各个相机类型基于重叠率和高度计算垄宽和速度
 */
public class OverlapRateUtil {
    /**
     * 相机类型
     */
    public enum CameraType{
        MAVIC_PRO,MAVIC_2_PRO,MAVIC_2,X4S,X5S,MAVIC_AIR,UNKNOWN
    }

    public enum ParamType{
        DRAW,//用重新画航线
        UNDRAW,//不用重新画航线
        SPEEDOUTOFRANGE,//速度超出范围
        SPACEOUTOFRANGE,//垄宽超出范围
        INRANGE
    }
    //御PRO
    private static final double MAVIC_PRO_FOV = 78.8;//视场角
    private static final double MAVIC_PRO_HORIZENTAL_PIX = 4000;//最大水平像素
    private static final double MAVIC_PRO_VERTICAL_PIX = 3000;//最大垂直像素
    //御2PRO
    private static final double MAVIC_2_PRO_FOV = 77;
    private static final double MAVIC_2_PRO_HORIZENTAL_PIX = 5472;
    private static final double MAVIC_2_PRO_VERTICAL_PIX = 3648;
    //御2
    private static final double MAVIC_2_FOV = 83;
    private static final double MAVIC_2_HORIZENTAL_PIX = 4000;
    private static final double MAVIC_2_VERTICAL_PIX = 3000;
    //X4S
    private static final double X4S_FOV = 84;
    private static final double X4S_HORIZENTAL_PIX = 5472;
    private static final double X4S_VERTICAL_PIX = 3648;
    //X5S
    private static final double X5S_FOV = 72;
    private static final double X5S_HORIZENTAL_PIX = 5280;
    private static final double X5S_VERTICAL_PIX = 3956;
    //御AIR
    private static final double MAVIC_AIR_FOV = 85;
    private static final double MAVIC_AIR_HORIZENTAL_PIX = 4056;
    private static final double MAVIC_AIR_VERTICAL_PIX = 3040;

    /**
     * 等效地面水平距离(m)
     * =TAN(C2/2*PI()/180)*F2*2/SQRT(D2*D2+E2*E2)*D2
     * C2:FOV
     * F2:altitude
     * D2:HORIZENTAL_PIX
     * E2:VERTICAL_PIC
     */
    private static double getHorizontalDistance(CameraType type,int altitude){
        double hd = 0;
        if(type==CameraType.MAVIC_2){
            hd = Math.tan(MAVIC_2_FOV/2*Math.PI/180)*altitude*2/Math.sqrt(MAVIC_2_HORIZENTAL_PIX*MAVIC_2_HORIZENTAL_PIX + MAVIC_2_VERTICAL_PIX * MAVIC_2_VERTICAL_PIX)*MAVIC_2_HORIZENTAL_PIX;
        }else if (type==CameraType.MAVIC_2_PRO){
            hd = Math.tan(MAVIC_2_PRO_FOV/2*Math.PI/180)*altitude*2/Math.sqrt(MAVIC_2_PRO_HORIZENTAL_PIX*MAVIC_2_PRO_HORIZENTAL_PIX + MAVIC_2_PRO_VERTICAL_PIX * MAVIC_2_PRO_VERTICAL_PIX)*MAVIC_2_PRO_HORIZENTAL_PIX;
        }else if (type==CameraType.MAVIC_PRO){
            hd = Math.tan(MAVIC_PRO_FOV/2*Math.PI/180)*altitude*2/Math.sqrt(MAVIC_PRO_HORIZENTAL_PIX*MAVIC_PRO_HORIZENTAL_PIX + MAVIC_PRO_VERTICAL_PIX * MAVIC_PRO_VERTICAL_PIX)*MAVIC_PRO_HORIZENTAL_PIX;
        }else if (type==CameraType.MAVIC_AIR){
            hd = Math.tan(MAVIC_AIR_FOV/2*Math.PI/180)*altitude*2/Math.sqrt(MAVIC_AIR_HORIZENTAL_PIX*MAVIC_AIR_HORIZENTAL_PIX + MAVIC_AIR_VERTICAL_PIX * MAVIC_AIR_VERTICAL_PIX)*MAVIC_AIR_HORIZENTAL_PIX;
        }else if (type==CameraType.X4S){
            hd = Math.tan(X4S_FOV/2*Math.PI/180)*altitude*2/Math.sqrt(X4S_HORIZENTAL_PIX*X4S_HORIZENTAL_PIX + X4S_VERTICAL_PIX * X4S_VERTICAL_PIX)*X4S_HORIZENTAL_PIX;
        }else if (type==CameraType.X5S){
            hd = Math.tan(X5S_FOV/2*Math.PI/180)*altitude*2/Math.sqrt(X5S_HORIZENTAL_PIX*X5S_HORIZENTAL_PIX + X5S_VERTICAL_PIX * X5S_VERTICAL_PIX)*X5S_HORIZENTAL_PIX;
        }
        return hd;
    }

    /**
     * 等效地面垂直距离(m)
     * =TAN(C2/2*PI()/180)*F2*2/SQRT(D2*D2+E2*E2)*E2
     * C2:FOV
     * F2:altitude
     * D2:HORIZENTAL_PIX
     * E2:VERTICAL_PIC
     */
    private static double getVerticalDistance(CameraType type,int altitude){
        double vd = 0;
        if(type==CameraType.MAVIC_2){
            vd = Math.tan(MAVIC_2_FOV/2*Math.PI/180)*altitude*2/Math.sqrt(MAVIC_2_HORIZENTAL_PIX*MAVIC_2_HORIZENTAL_PIX + MAVIC_2_VERTICAL_PIX * MAVIC_2_VERTICAL_PIX)*MAVIC_2_VERTICAL_PIX;
        }else if (type==CameraType.MAVIC_2_PRO){
            vd = Math.tan(MAVIC_2_PRO_FOV/2*Math.PI/180)*altitude*2/Math.sqrt(MAVIC_2_PRO_HORIZENTAL_PIX*MAVIC_2_PRO_HORIZENTAL_PIX + MAVIC_2_PRO_VERTICAL_PIX * MAVIC_2_PRO_VERTICAL_PIX)*MAVIC_2_PRO_VERTICAL_PIX;
        }else if (type==CameraType.MAVIC_PRO){
            vd = Math.tan(MAVIC_PRO_FOV/2*Math.PI/180)*altitude*2/Math.sqrt(MAVIC_PRO_HORIZENTAL_PIX*MAVIC_PRO_HORIZENTAL_PIX + MAVIC_PRO_VERTICAL_PIX * MAVIC_PRO_VERTICAL_PIX)*MAVIC_PRO_VERTICAL_PIX;
        }else if (type==CameraType.MAVIC_AIR){
            vd = Math.tan(MAVIC_AIR_FOV/2*Math.PI/180)*altitude*2/Math.sqrt(MAVIC_AIR_HORIZENTAL_PIX*MAVIC_AIR_HORIZENTAL_PIX + MAVIC_AIR_VERTICAL_PIX * MAVIC_AIR_VERTICAL_PIX)*MAVIC_AIR_VERTICAL_PIX;
        }else if (type==CameraType.X4S){
            vd = Math.tan(X4S_FOV/2*Math.PI/180)*altitude*2/Math.sqrt(X4S_HORIZENTAL_PIX*X4S_HORIZENTAL_PIX + X4S_VERTICAL_PIX * X4S_VERTICAL_PIX)*X4S_VERTICAL_PIX;
        }else if (type==CameraType.X5S){
            vd = Math.tan(X5S_FOV/2*Math.PI/180)*altitude*2/Math.sqrt(X5S_HORIZENTAL_PIX*X5S_HORIZENTAL_PIX + X5S_VERTICAL_PIX * X5S_VERTICAL_PIX)*X5S_VERTICAL_PIX;
        }
        return vd;
    }

    /**
     * 垄宽
     * =G2-L2*G2/100
     * G2:等效地面水平距离(m)
     * L2:重叠率
     * @return
     */
    public static int getSpace(String camearName,int altitude,int overlapRate){
        CameraType type = CameraType.UNKNOWN;
        if (TextUtils.isEmpty(camearName)){
            type = CameraType.MAVIC_PRO;
        }else if (camearName.equalsIgnoreCase("")){
            type = CameraType.MAVIC_2;
        }else if (camearName.equalsIgnoreCase("")){
            type = CameraType.MAVIC_2_PRO;
        }else if (camearName.equalsIgnoreCase("")){
            type = CameraType.MAVIC_PRO;
        }else if (camearName.equalsIgnoreCase("")){
            type = CameraType.MAVIC_AIR;
        }else if (camearName.equalsIgnoreCase("")){
            type = CameraType.X4S;
        }else if (camearName.equalsIgnoreCase("ZENMUSE X5S")){
            type = CameraType.X5S;
        }else{
            type = CameraType.MAVIC_PRO;
        }
        double hd = getHorizontalDistance(type,altitude);
        int space = (int)(hd-hd*overlapRate/100);
        return space;
    }

    /**
     * 速度
     * =(H2-Q2*H2/100)/2
     * H2:等效地面垂直距离(m)
     * Q2:重叠率
     * @return
     */
    public static float getSpeed(String camearName,int altitude,int overlapRate){
        CameraType type = CameraType.UNKNOWN;
        if (TextUtils.isEmpty(camearName)){
            type = CameraType.MAVIC_PRO;
        }else if (camearName.equalsIgnoreCase("")){
            type = CameraType.MAVIC_2;
        }else if (camearName.equalsIgnoreCase("")){
            type = CameraType.MAVIC_2_PRO;
        }else if (camearName.equalsIgnoreCase("")){
            type = CameraType.MAVIC_PRO;
        }else if (camearName.equalsIgnoreCase("")){
            type = CameraType.MAVIC_AIR;
        }else if (camearName.equalsIgnoreCase("")){
            type = CameraType.X4S;
        }else if (camearName.equalsIgnoreCase("ZENMUSE X5S")){
            type = CameraType.X5S;
        }else{
            type = CameraType.MAVIC_PRO;
        }
        double vd = getVerticalDistance(type,altitude);
        double speed = (vd-vd*overlapRate/100)/2;
        DecimalFormat fnum = new DecimalFormat("#.#");
        String spd = fnum.format(speed);
        return Float.parseFloat(spd);
    }
}

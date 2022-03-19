package com.cimraffi.android.birdeye.utils;

import android.util.Log;

public class SettingUtil {
	static double[] fov_arr = {78.8, 72, 57.2,77};
	static int[] pix_shuiping_arr = {4000, 5280, 1600,5472};
	static int[] pix_chuizhi_arr = {3000, 3956, 1300,3648};

	/*
	public static double longkuan(double h, double chongdie) {
		//TAN(B3/2*PI()/180)*B6*2/SQRT(B4*B4+B5*B5)*B4
		double dist_shuiping = Math.tan(fov/2*Math.PI/180)*h*2/Math.sqrt(pix_shuiping*pix_shuiping+pix_chuizhi*pix_chuizhi)*pix_shuiping;
		double dist_chuizhi = Math.tan(fov/2*Math.PI/180)*h*2/Math.sqrt(pix_shuiping*pix_shuiping+pix_chuizhi*pix_chuizhi)*pix_chuizhi;
		double longkuan = dist_shuiping*(100-chongdie*100)/100.0;
		return longkuan;
	}

	public static double sudu(double h, double chongdie, double shoot_jiange) {
		//TAN(B3/2*PI()/180)*B6*2/SQRT(B4*B4+B5*B5)*B4
		double dist_shuiping = Math.tan(fov/2*Math.PI/180)*h*2/Math.sqrt(pix_shuiping*pix_shuiping+pix_chuizhi*pix_chuizhi)*pix_shuiping;
		double dist_chuizhi = Math.tan(fov/2*Math.PI/180)*h*2/Math.sqrt(pix_shuiping*pix_shuiping+pix_chuizhi*pix_chuizhi)*pix_chuizhi;
		double sudo = dist_chuizhi*(100-chongdie*100)/(100.0*shoot_jiange);
		return sudo;
	}*/

	public static double overlap_front(double h, double speed, double shoot_jiange, int camera_type) {
		double fov = fov_arr[camera_type];
		int pix_shuiping = pix_shuiping_arr[camera_type];
		int pix_chuizhi = pix_chuizhi_arr[camera_type];
		double dist_chuizhi = Math.tan(fov/2*Math.PI/180)*h*2/Math.sqrt(pix_shuiping*pix_shuiping+pix_chuizhi*pix_chuizhi)*pix_chuizhi;
		double dist_shoot = speed * shoot_jiange;
		Log.d("overlap", String.format("overlap_front dist_shoot:%f dist_chuizhi:%f", dist_shoot, dist_chuizhi));
		double overlap_front = (dist_shoot >= dist_chuizhi)?0:(dist_chuizhi-dist_shoot)*100/dist_chuizhi;
		return overlap_front;
	}

	public static double overlap_side(double h, double space, int camera_type) {
		double fov = fov_arr[camera_type];
		int pix_shuiping = pix_shuiping_arr[camera_type];
		int pix_chuizhi = pix_chuizhi_arr[camera_type];
		double dist_shuiping = Math.tan(fov/2*Math.PI/180)*h*2/Math.sqrt(pix_shuiping*pix_shuiping+pix_chuizhi*pix_chuizhi)*pix_shuiping;
		Log.d("overlap", String.format("overlap_side space:%f dist_shuiping:%f", space, dist_shuiping));
		double overlap_side = (space >= dist_shuiping)?0:(dist_shuiping-space)*100/dist_shuiping;
		return overlap_side;
	}
}

package com.cimraffi.android.birdeye.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.cimraffi.android.birdeye.bean.DeviceList;
import com.cimraffi.android.birdeye.R;
import java.util.ArrayList;

public class DeviceAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<DeviceList.DeviceData> list;

    public DeviceAdapter(Context context, ArrayList<DeviceList.DeviceData> list){
        this.mContext = context;
        this.list = list;
    }
    @Override
    public int getCount() {
        return list==null?0:list.size();
    }

    @Override
    public DeviceList.DeviceData getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView= LayoutInflater.from(mContext).inflate(R.layout.activity_birdeye_spinner_white_text, null);
        TextView tvgetView=(TextView) convertView.findViewById(android.R.id.text1);
        tvgetView.setText(getItem(position).name);
        tvgetView.setTextSize(14);
        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        convertView= LayoutInflater.from(mContext).inflate(android.R.layout.simple_list_item_single_choice, null);
        TextView tvgetView=(TextView) convertView.findViewById(android.R.id.text1);
        tvgetView.setText(getItem(position).name);
        return convertView;
    }
}

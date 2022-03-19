package com.airbnb.android.airmapview.listeners;

import com.airbnb.android.airmapview.MapUtils.LatLng;

public interface OnPositionCallback {
  void onPositionChange(LatLng positon);
}

package com.airbnb.android.airmapview.listeners;

import com.airbnb.android.airmapview.MapUtils.LatLng;

import java.util.List;

public interface OnGenAirwayCallback {
  void onAirwayReady(List<LatLng> points);
}

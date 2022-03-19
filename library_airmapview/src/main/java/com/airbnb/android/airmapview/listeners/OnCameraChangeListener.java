package com.airbnb.android.airmapview.listeners;

import com.airbnb.android.airmapview.MapUtils.LatLng;

public interface OnCameraChangeListener {
  void onCameraChanged(LatLng latLng, int zoom);
}

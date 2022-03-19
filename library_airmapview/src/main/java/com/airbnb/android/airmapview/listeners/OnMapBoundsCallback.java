package com.airbnb.android.airmapview.listeners;

import com.airbnb.android.airmapview.MapUtils.LatLngBounds;

public interface OnMapBoundsCallback {
  void onMapBoundsReady(LatLngBounds bounds);
}

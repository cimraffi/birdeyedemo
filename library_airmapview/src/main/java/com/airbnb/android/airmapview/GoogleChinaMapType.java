package com.airbnb.android.airmapview;

public class GoogleChinaMapType extends AirMapType {

  public GoogleChinaMapType() {
    super("google_map.html", "google_map.html", "www.google.cn");
  }
  public GoogleChinaMapType(boolean inCN) {
    super(inCN);
  }
}
